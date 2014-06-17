import os
import re
import time
import datetime

from sfa.util.faults import *
from sfa.util.sfalogging import logger
from sfa.util.config import Config
from sfa.util.callids import Callids
from sfa.util.version import version_core
from sfa.util.xrn import urn_to_hrn, hrn_to_urn, Xrn
from sfa.util.plxrn import hrn_to_pl_slicename, PlXrn

# xxx the sfa.rspecs module is dead - this symbol is now undefined
#from sfa.rspecs.sfa_rspec import sfa_rspec_version

from sfa.managers.aggregate_manager import AggregateManager

from sfa.plc.plslices import PlSlices

from sfa.trust.credential import Credential

class AggregateManagerMax (AggregateManager):

    def __init__ (self, config):
        pass

    RSPEC_TMP_FILE_PREFIX = "/var/tmp/max_rspec"
    
    # execute shell command and return both exit code and text output
    def shell_execute(self, cmd, timeout):
        pipe = os.popen(cmd + ' 2>&1', 'r')
        text = ''
        while timeout:
            line = pipe.read()
            text += line
            time.sleep(1)
            timeout = timeout-1
        code = pipe.close()
        if code is None: code = 0
        while (text[-1:] == '\n' or text[-1:] == '\r'): 
            text = text[:-1]
        return code, text
    
   
    def call_am_apiclient(self, client_app, params, timeout):
        """
        call AM API client with command like in the following example:
        cd aggregate_client; java -classpath AggregateWS-client-api.jar:lib/* \
          net.geni.aggregate.client.examples.CreateSliceNetworkClient \
          ./repo https://geni:8443/axis2/services/AggregateGENI \
          ... params ...
        """
        (client_path, am_url) = Config().get_max_aggrMgr_info()
        sys_cmd = "cd " + client_path + "; java -classpath AggregateWS-client-api.jar:lib/* net.geni.aggregate.client.examples." + client_app + " ./repo " + am_url + " " + ' '.join(params)
        ret = self.shell_execute(sys_cmd, timeout)
        logger.debug("shell_execute cmd: %s returns %s" % (sys_cmd, ret))
        return ret
    
    # save request RSpec xml content to a tmp file
    def save_rspec_to_file(self, rspec):
        path = AggregateManagerMax.RSPEC_TMP_FILE_PREFIX + "_" + \
            time.strftime('%Y%m%dT%H:%M:%S', time.gmtime(time.time())) +".xml"
        file = open(path, "w")
        file.write(rspec)
        file.close()
        return path
    
    # get stripped down slice id/name plc.maxpl.xislice1 --> maxpl_xislice1
    def get_plc_slice_id(self, cred, xrn):
        xrn = Xrn(xrn)
        hrn = xrn.get_hrn()  	
        hrn = hrn.replace('\.', '')
        if hrn.find(':') != -1:
            sep=':'
        elif hrn.find('+') != -1:
            sep='+'
        else:
            sep='.'
        slice_id = hrn.split(sep)[-2] + '_' + hrn.split(sep)[-1]
        return slice_id
    
    # extract xml 
    def get_xml_by_tag(self, text, tag):
        indx1 = text.find('<'+tag)
        indx2 = text.find('/'+tag+'>')
        xml = None
        if indx1!=-1 and indx2>indx1:
            xml = text[indx1:indx2+len(tag)+2]
        return xml

    def extract_exception(self, text):
        indx1 = text.find('Exception')
        if indx1!=-1:
            indx2 = text.find('\n', indx1)
            if indx2>indx1:
                return text[indx1:indx2]
            else:
                return text[indx:]
        return None

    # formerly in aggregate_manager.py but got unused in there...    
    def _get_registry_objects(self, slice_xrn, creds, users):
        """
    
        """
        xrn = Xrn(slice_xrn)
        hrn = xrn.get_hrn()		        	
    
        #hrn_auth = get_authority(hrn)
    
        # Build up objects that an SFA registry would return if SFA
        # could contact the slice's registry directly
        reg_objects = None
    
        if users:
            # dont allow special characters in the site login base
            #only_alphanumeric = re.compile('[^a-zA-Z0-9]+')
            #login_base = only_alphanumeric.sub('', hrn_auth[:20]).lower()
            slicename = hrn_to_pl_slicename(hrn)
            login_base = slicename.split('_')[0]
            reg_objects = {}
            site = {}
            site['site_id'] = 0
            site['name'] = 'geni.%s' % login_base 
            site['enabled'] = True
            site['max_slices'] = 100
    
            # Note:
            # Is it okay if this login base is the same as one already at this myplc site?
            # Do we need uniqueness?  Should use hrn_auth instead of just the leaf perhaps?
            site['login_base'] = login_base
            site['abbreviated_name'] = login_base
            site['max_slivers'] = 1000
            reg_objects['site'] = site
    
            slice = {}
            
            # get_expiration always returns a normalized datetime - no need to utcparse
            extime = Credential(string=creds[0]).get_expiration()
            # If the expiration time is > 60 days from now, set the expiration time to 60 days from now
            if extime > datetime.datetime.utcnow() + datetime.timedelta(days=60):
                extime = datetime.datetime.utcnow() + datetime.timedelta(days=60)
            slice['expires'] = int(time.mktime(extime.timetuple()))
            slice['hrn'] = hrn
            slice['name'] = hrn_to_pl_slicename(hrn)
            slice['url'] = hrn
            slice['description'] = hrn
            slice['pointer'] = 0
            reg_objects['slice_record'] = slice
    
            reg_objects['users'] = {}
            for user in users:
                user['key_ids'] = []
                xrn = Xrn(user['urn'])
                hrn = xrn.get_hrn()		
                user['email'] = hrn_to_pl_slicename(hrn) + "@geni.net"
                user['first_name'] = hrn
                user['last_name'] = hrn
                reg_objects['users'][user['email']] = user
    
            return reg_objects
    
    def prepare_slice(self, api, slice_xrn, creds, users):
        reg_objects = self._get_registry_objects(slice_xrn, creds, users)
        xrn = Xrn(slice_xrn)
        hrn = xrn.get_urn()
        	
        slices = PlSlices(self.driver)
        peer = slices.get_peer(hrn)
        sfa_peer = slices.get_sfa_peer(hrn)
        slice_record=None
        if users:
            slice_record = users[0].get('slice_record', {})
        registry = api.registries[api.hrn]
        credential = api.getCredential()
        # translate hrn to remove 'hyphen'
	hrn = re.sub('-', '', hrn)
        # ensure site record exists
        #site = slices.verify_site(hrn, slice_record, peer, sfa_peer)
        # ensure slice record exists
        #slice = slices.verify_slice(hrn, slice_record, peer, sfa_peer)
        # ensure person records exists
        #persons = slices.verify_persons(hrn, slice, users, peer, sfa_peer)
    
    def parse_resources(self, text, slice_xrn):
        resources = []
        urn = hrn_to_urn(slice_xrn, 'sliver')
        plc_slice = re.search("Slice Status => ([^\n]+)", text)
        if plc_slice and plc_slice.group and plc_slice.group(1) != 'NONE':
            res = {}
            res['geni_urn'] = urn + '_plc_slice'
            res['geni_error'] = ''
            res['geni_status'] = 'unknown'
            if plc_slice.group(1) == 'CREATED':
                res['geni_status'] = 'ready'
            resources.append(res)
        vlans = re.findall("GRI => ([^\n]+)\n\t  Status => ([^\n]+)\n\t  Message => ([^\n]*)\n\t", text)
        for vlan in vlans:
            res = {}
            res['geni_error'] = ''
            res['geni_urn'] = urn + '_vlan_' + vlan[0]
            if vlan[1] == 'ACTIVE':
                res['geni_status'] = 'ready'
            elif vlan[1] == 'FAILED' or vlan[1] == 'UNKNOWN':
                res['geni_status'] = 'failed'
                res['geni_error'] = vlan[2]
            elif vlan[1] == 'CANCELLED':
                res['geni_status'] = 'failed'
                res['geni_error'] = 'VLAN cancelled by rollback from contingent failure'
            else:
                res['geni_status'] = 'changing'
            resources.append(res)
        return resources
    
    def slice_status(self, api, slice_xrn, creds):
        #logger.info("SLICE STATUS: URN (BEFORE): %s" % slice_xrn)
        urn = PlXrn(xrn=slice_xrn, type='slice').get_urn()		
        result = {}
        top_level_status = 'unknown'
        #slice_id = self.get_plc_slice_id(creds, urn)
        slice_id = urn
        #logger.info("SLICE STATUS: URN (After): %s" % slice_xrn)
        (ret, output) = self.call_am_apiclient("QuerySliceNetworkClient", [slice_id,], 5)
        # parse output into rspec XML
        if output.find("Unkown Rspec:") > 0:
            raise NonExistingRecord(slice_id)
        else:
            if output.find("Status => FAILED") > 0 or output.find("Status => UNKNOWN") > 0:
                top_level_status = 'failed'
            elif (output.find("Status => ACCEPTED") > 0 or output.find("Status => PENDING") > 0
                  or output.find("Status => IN") > 0 or output.find("Status => PATH") > 0
                  or output.find("Status => COMMITTED") > 0 
                 ):
                top_level_status = 'changing'
            else:
                top_level_status = 'ready'
            result['geni_resources'] = self.parse_resources(output, slice_xrn)
            if not result['geni_resources']:
                top_level_status = 'failed'
        result['geni_urn'] = urn
        result['geni_status'] = top_level_status
        expires = re.search("Expires => ([^\n]*)", output)
        if expires:
             result['geni_expires'] = expires.group(1)
        return result
    
    def create_slice(self, api, xrn, cred, rspec, users):
        indx1 = rspec.find("<RSpec")
        indx2 = rspec.find("</RSpec>")
        if indx1 > -1 and indx2 > indx1:
            rspec = rspec[indx1+len("<RSpec type=\"SFA\">"):indx2-1]
        rspec_path = self.save_rspec_to_file(rspec)
        self.prepare_slice(api, xrn, cred, users)
        #slice_id = self.get_plc_slice_id(cred, xrn)
        slice_id = xrn
        sys_cmd = "sed -i \"s/rspec id=\\\"[^\\\"]*/rspec id=\\\"" +slice_id+ "/g\" " + rspec_path + ";sed -i \"s/:rspec=[^:'<\\\" ]*/:rspec=" +slice_id+ "/g\" " + rspec_path
        ret = self.shell_execute(sys_cmd, 1)
        sys_cmd = "sed -i \"s/rspec id=\\\"[^\\\"]*/rspec id=\\\"" + rspec_path + "/g\""
        ret = self.shell_execute(sys_cmd, 1)
        (ret, output) = self.call_am_apiclient("CreateSliceNetworkClient", [slice_id, rspec_path,], 3)
        # parse output for error
        if output.find("already existed") > 0:
            raise ExistingRecord(slice_id)
        elif output.find("Rspec parsing failed") > 0 or output.find("Rspec needs to have rspecName") > 0:
            raise InvalidRSpec(slice_id)
        elif output.find("AxisFault") > 0:
            raise SfaAPIError("Unknown AM internal error")
        elif output.find("Status => FAILED") > 0:
            errmsg = re.search("Message => ([^\n]+)", output)
            if errmsg == None:
                raise SfaAPIError('Unknown internal failure')
            if errmsg.group(1) != None:
                raise SfaAPIError(errmsg.group(1))
        elif output.find("Exception") > 0:
            error = self.extract_exception(output)
            raise SfaAPIError(error)
        return True
    
    def delete_slice(self, api, xrn, cred):
        #slice_id = self.get_plc_slice_id(cred, xrn)
        logger.info("DELETE SLICE: %s" % xrn)
        slice_id = xrn
        (ret, output) = self.call_am_apiclient("DeleteSliceNetworkClient", [slice_id,], 3)
        if output.find("Unkown Rspec:") > 0:
            raise NonExistingRecord(slice_id)
        elif output.find("Exception") > 0:
            raise SfaAPIError(self.extract_exception(output))
        return 1
    
    
    def get_rspec(self, api, cred, slice_urn):
        logger.debug("#### called max-get_rspec")
        #geni_slice_urn: urn:publicid:IDN+plc:maxpl+slice+xi_rspec_test1
        if slice_urn == None:
            (ret, output) = self.call_am_apiclient("GetResourceTopology", ['all', '\"\"'], 5)
        else:
            #slice_id = self.get_plc_slice_id(cred, slice_urn)
            slice_id = slice_urn
            (ret, output) = self.call_am_apiclient("GetResourceTopology", ['all', slice_id,], 5)
        # parse output into rspec XML
        if output.find("No resource found") > 0:
            raise NonExistingRecord(slice_id)
        elif output.find("Exception") > 0:
            raise SfaAPIError(self.extract_exception(output))
        else:
            geni_rspec = self.get_xml_by_tag(output, 'rspec')
            if geni_rspec != None:
                rspec = geni_rspec
            else:
                rspec = "<rspec>"
                comp_rspec = self.get_xml_by_tag(output, 'computeResource')
                logger.debug("#### computeResource %s" % comp_rspec)
                topo_rspec = self.get_xml_by_tag(output, 'topology')
                logger.debug("#### topology %s" % topo_rspec)
                if comp_rspec != None:
                    rspec = rspec + comp_rspec
                if topo_rspec != None:
                    rspec = rspec + topo_rspec
                rspec = "</rspec>"
        return (rspec)
    
    def start_slice(self, api, xrn, cred):
        # service not supported
        return None
    
    def renew_slice(self, api, xrn, cred, expiration_time):
        slice_id = xrn
        (ret, output) = self.call_am_apiclient("RenewSliceNetworkClient", [slice_id, expiration_time], 3)
        if output.find("Unkown Rspec:") > 0:
            raise NonExistingRecord(slice_id)
        elif output.find("Status => FAILED") > 0:
            errmsg = re.search("Message => ([^\n]+)", output)
            if errmsg == None:
                raise SfaAPIError('Unknown AM internal failure')
            if errmsg.group(1) != None:
                raise SfaAPIError(errmsg.group(1))
        elif output.find("Exception") > 0:
            raise SfaAPIError(self.extract_exception(output))
        elif output.find("Error") > 0:
            raise SfaAPIError('Unknown SFA failure')
        return 1

    def stop_slice(self, api, xrn, cred, options):
        #slice_id = self.get_plc_slice_id(cred, xrn)
        slice_id = xrn
        (ret, output) = self.call_am_apiclient("DeleteSliceNetworkClient", [slice_id,], 3)
        if output.find("Unkown Rspec:") > 0:
            raise NonExistingRecord(slice_id)
        elif output.find("Exception") > 0:
            raise SfaAPIError(self.extract_exception(output))
        return 1
    
    def reset_slices(self, api, xrn):
        # service not supported
        return None
    
    ### GENI AM API Methods
    
    def GetVersion(self, api, options):
        xrn=Xrn(api.hrn)
        version = version_core()
        hrn=xrn.get_hrn()
        urn = 'urn:publicid:IDN+' + hrn + '+authority+am'
        version_generic = {
            'interface':'aggregate',
            'sfa': 2,
            'geni_api': 2,
            'geni_api_versions': {'2': 'http://%s:%s' % (api.config.SFA_AGGREGATE_HOST, api.config.SFA_AGGREGATE_PORT)},
            'hrn': hrn,
            'urn': urn,
            'geni_am_type': ['dcn','max'],
            }
        version.update(version_generic)
        testbed_version = self.driver.aggregate_version()
        version.update(testbed_version)
        return version

    def SliverStatus(self, api, slice_xrn, creds, options):
        call_id = options.get('call_id')
        if Callids().already_handled(call_id): return {}
        return self.slice_status(api, slice_xrn, creds)
    
    def CreateSliver(self, api, slice_xrn, creds, rspec_string, users, options):
        call_id = options.get('call_id')
        if Callids().already_handled(call_id): return ""
        #TODO: create real CreateSliver response rspec
        ret = self.create_slice(api, slice_xrn, creds, rspec_string, users)
        if ret:
            # wait some time for circuit ID to show up in query
            time.sleep(30)
            ret = self.get_rspec(api, creds, slice_xrn)
            gri = self.get_xml_by_tag(ret, 'globalId')
            rest_of_ret = ret
            while gri:
                tokens = gri.split('<')
                gri = tokens[1].split('>')[1]
                creatorUrn = Credential(string=creds[0]).gidCaller.get_urn()
                creatorEmail = Credential(string=creds[0]).gidCaller.get_email()
                sliceEmail = Credential(string=creds[0]).gidObject.get_email()
                logger.info("NOCReport{CreateSliver: CircuitID='%s',SliceURN='%s',CreatorUrn='%s',CreatorEmail='%s',SliceEmail='%s'}" % (gri, slice_xrn, creatorUrn, creatorEmail, sliceEmail))
                rest_of_ret = rest_of_ret[rest_of_ret.find('<globalId>'+gri)+(10+len(gri)):]
                gri = self.get_xml_by_tag(rest_of_ret, 'globalId')
            return ret
        else:
            return "<?xml version=\"1.0\" ?> <rspec type=\"manifest\"> Error! </rspec>"
    
    def DeleteSliver(self, api, xrn, creds, options):
        call_id = options.get('call_id')
        if Callids().already_handled(call_id): return ""
        return self.delete_slice(api, xrn, creds)
    
    def RenewSliver(self, api, xrn, creds, expiration_time, options):
        call_id = options.get('call_id')
        if Callids().already_handled(call_id): return ""
        return self.renew_slice(api, xrn, creds, expiration_time)

    # no caching
    def ListResources(self, api, creds, options):
        call_id = options.get('call_id')
        if Callids().already_handled(call_id): return ""
        # version_string = "rspec_%s" % (rspec_version.get_version_name())
        slice_urn = options.get('geni_slice_urn')
        return self.get_rspec(api, creds, slice_urn)
    
    def fetch_context(self, slice_hrn, user_hrn, contexts):
        """
        Returns the request context required by sfatables. At some point, this mechanism should be changed
        to refer to "contexts", which is the information that sfatables is requesting. But for now, we just
        return the basic information needed in a dict.
        """
        base_context = {'sfa':{'user':{'hrn':user_hrn}}}
        return base_context

