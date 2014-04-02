#
# implements support for SFA records stored in db tables
#
# TODO: Use existing PLC database methods? or keep this separate?

from types import StringTypes

from sfa.util.config import Config

from sfa.storage.parameter import Parameter
from sfa.storage.filter import Filter
from sfa.storage.PostgreSQL import PostgreSQL
from sfa.storage.record import SfaRecord, AuthorityRecord, NodeRecord, SliceRecord, UserRecord

class SfaTable(list):

    SFA_TABLE_PREFIX = "records"

    def __init__(self, record_filter = None):

        # pgsql doesn't like table names with "." in them, to replace it with "$"
        self.tablename = SfaTable.SFA_TABLE_PREFIX
        self.config = Config()
        self.db = PostgreSQL(self.config)

        if record_filter:
            records = self.find(record_filter)
            for record in records:
                self.append(record)             

    def db_fields(self, obj=None):
        
        db_fields = self.db.fields(self.SFA_TABLE_PREFIX)
        return dict( [ (key,value) for (key, value) in obj.iteritems() \
                        if key in db_fields and
                        self.is_writable(key, value, SfaRecord.fields)] )      

    @staticmethod
    def is_writable (key,value,dict):
        # if not mentioned, assume it's writable (e.g. deleted ...)
        if key not in dict: return True
        # if mentioned but not linked to a Parameter object, idem
        if not isinstance(dict[key], Parameter): return True
        # if not marked ro, it's writable
        if not dict[key].ro: return True

        return False


    def clear (self):
        self.db.do("DELETE from %s"%self.tablename)
        self.db.commit()

    # what sfa-nuke does
    def nuke (self):
        self.clear()

    def remove(self, record):
        params = {'record_id': record['record_id']}
        template = "DELETE FROM %s " % self.tablename
        sql = template + "WHERE record_id = %(record_id)s"
        self.db.do(sql, params)
        
        # if this is a site, remove all records where 'authority' == the 
        # site's hrn
        if record['type'] == 'authority':
            params = {'authority': record['hrn']}
            sql = template + "WHERE authority = %(authority)s"
            self.db.do(sql, params)
        self.db.commit() 

    def insert(self, record):
        db_fields = self.db_fields(record)
        keys = db_fields.keys()
        values = [self.db.param(key, value) for (key, value) in db_fields.iteritems()]
        query_str = "INSERT INTO " + self.tablename + \
                       "(" + ",".join(keys) + ") " + \
                       "VALUES(" + ",".join(values) + ")"
        self.db.do(query_str, db_fields)
        self.db.commit()
        result = self.find({'hrn': record['hrn'], 'type': record['type'], 'peer_authority': record['peer_authority']})
        if not result:
            record_id = None
        elif isinstance(result, list):
            record_id = result[0]['record_id']
        else:
            record_id = result['record_id']

        return record_id

    def update(self, record):
        db_fields = self.db_fields(record)
        keys = db_fields.keys()
        values = [self.db.param(key, value) for (key, value) in db_fields.iteritems()]
        columns = ["%s = %s" % (key, value) for (key, value) in zip(keys, values)]
        query_str = "UPDATE %s SET %s WHERE record_id = %s" % \
                    (self.tablename, ", ".join(columns), record['record_id'])
        self.db.do(query_str, db_fields)
        self.db.commit()

    def quote_string(self, value):
        return str(self.db.quote(value))

    def quote(self, value):
        return self.db.quote(value)

    def find(self, record_filter = None, columns=None):
        if not columns:
            columns = "*"
        else:
            columns = ",".join(columns)
        sql = "SELECT %s FROM %s WHERE True " % (columns, self.tablename)
        
        if isinstance(record_filter, (list, tuple, set)):
            ints = filter(lambda x: isinstance(x, (int, long)), record_filter)
            strs = filter(lambda x: isinstance(x, StringTypes), record_filter)
            record_filter = Filter(SfaRecord.all_fields, {'record_id': ints, 'hrn': strs})
            sql += "AND (%s) %s " % record_filter.sql("OR") 
        elif isinstance(record_filter, dict):
            record_filter = Filter(SfaRecord.all_fields, record_filter)        
            sql += " AND (%s) %s" % record_filter.sql("AND")
        elif isinstance(record_filter, StringTypes):
            record_filter = Filter(SfaRecord.all_fields, {'hrn':[record_filter]})    
            sql += " AND (%s) %s" % record_filter.sql("AND")
        elif isinstance(record_filter, int):
            record_filter = Filter(SfaRecord.all_fields, {'record_id':[record_filter]})    
            sql += " AND (%s) %s" % record_filter.sql("AND")

        results = self.db.selectall(sql)
        if isinstance(results, dict):
            results = [results]
        return results

    def findObjects(self, record_filter = None, columns=None):
        
        results = self.find(record_filter, columns) 
        result_rec_list = []
        for result in results:
            if result['type'] in ['authority']:
                result_rec_list.append(AuthorityRecord(dict=result))
            elif result['type'] in ['node']:
                result_rec_list.append(NodeRecord(dict=result))
            elif result['type'] in ['slice']:
                result_rec_list.append(SliceRecord(dict=result))
            elif result['type'] in ['user']:
                result_rec_list.append(UserRecord(dict=result))
            else:
                result_rec_list.append(SfaRecord(dict=result))
        return result_rec_list


