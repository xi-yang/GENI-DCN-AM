#!/usr/bin/python
#
# Wrapper around xmlrpclib for interfacing with PLCAPI servers
#
# Mark Huang <mlhuang@cs.princeton.edu>
# Copyright (C) 2005 The Trustees of Princeton University
#
# $Id: plcapilib.py,v 1.11 2006/04/10 15:42:12 mlhuang Exp $
#

import getpass
import getopt
import os, sys
import xmlrpclib
import re
import inspect
import pydoc

class PLCAPI:
    def usage(self, moreusage = ""):
        print moreusage + """
API options:
	-h host		API URL (default: %s)
	-f file    	API constants file (default: %s)
        -m method       API authentication method (default: %s)
	-p password	API password
	-u username	API user name
	-r role         API role
        -v              Be verbose
""" % (self.server_url,
       self.constants_path,
       self.constants['PL_API_CAPABILITY_AUTH_METHOD'])

    def __init__(self):
        # Debug
        self.verbose = 0
        
        # API default constants file
        self.constants_path = "/etc/planetlab/plc_api"

        # API default constants (if file does not exist)
        self.constants = {
            'PL_API_SERVER': "www.planet-lab.org",
            'PL_API_PATH': "/PLCAPI/",
            'PL_API_PORT': 443,
            'PL_API_CAPABILITY_AUTH_METHOD': "password",
            'PL_API_CAPABILITY_PASS': "",
            'PL_API_CAPABILITY_USERNAME': ""
        }
        self.server_url = "https://www.planet-lab.org/PLCAPI/"
        self.role = None

        # Multicall
        self.calls = []
        self.multi = False

    def getopt(self, args, shortopts = "", longopts = [], moreusage = ""):
        # (Re)parse constants file if available
        self.parse_constants_file(self.constants_path)

        # Standard API options
        shortopts += "h:f:m:p:u:r:v"
        longopts += ["host=", "constants=", "method=", "password=", "username=", "role=", "verbose", "help"]

        try:
            (opts, argv) = getopt.getopt(args[1:], shortopts, longopts)
        except getopt.GetoptError, err:
            print "Error: " + err.msg
            self.usage(moreusage)
            sys.exit(1)
            
        moreopts = {}
        for (opt, optval) in opts:
            if opt == "-h" or opt == "--host":
                self.server_url = optval
            elif opt == "-f" or opt == "--constants":
                self.parse_constants_file(optval)
            elif opt == "-m" or opt == "--method":
                self.constants['PL_API_CAPABILITY_AUTH_METHOD'] = optval
            elif opt == "-p" or opt == "--password":
                self.constants['PL_API_CAPABILITY_PASS'] = optval
            elif opt == "-u" or opt == "--username":
                self.constants['PL_API_CAPABILITY_USERNAME'] = optval
            elif opt == "-r" or opt == "--role":
                self.role = optval
            elif opt == "-v" or opt == "--verbose":
                self.verbose += 1
            elif opt == "--help":
                self.usage(moreusage)
                sys.exit(0)
            else:
                moreopts[opt] = optval

        # Capability authentication only available to admins
        if self.role is None and \
           self.constants['PL_API_CAPABILITY_AUTH_METHOD'] == "capability":
            self.role = "admin"

        # Both a role and a username must be specified if not anonymous
        if self.role is None or \
           self.role != "anonymous" and not self.constants['PL_API_CAPABILITY_USERNAME']:
            if self.role is None:
                print "Error: must specify a role with -r"
            else:
                print "Error: must specify a username with -u"
            self.usage(moreusage)
            sys.exit(1)

        # Password must be specified if not anonymous
        if self.role != "anonymous" and not self.constants['PL_API_CAPABILITY_PASS']:
            try:
                self.constants['PL_API_CAPABILITY_PASS'] = getpass.getpass()
            except (EOFError, KeyboardInterrupt):
                print
                sys.exit(0)

        # Setup authentication structs
        self.anon = {
            'AuthMethod': "anonymous"
        }
        if self.role == "anonymous":
            self.auth = self.anon
        else:
            self.auth = {
                'Username': self.constants['PL_API_CAPABILITY_USERNAME'],
                'AuthMethod': self.constants['PL_API_CAPABILITY_AUTH_METHOD'],
                'AuthString': self.constants['PL_API_CAPABILITY_PASS'],
                'Role': self.role
            }

        # Connect to API server
        self.server = xmlrpclib.Server(self.server_url, verbose = self.verbose)
        if self.role != "anonymous":
            try:
                self.server.AuthCheck(self.auth)
            except xmlrpclib.Fault, fault:
                print fault
                sys.exit(fault.faultCode)

        # Save pointer to built-in Python help function
        self.python_help = help

        return (moreopts, argv)

    def parse_constants_file(self, path):
        """Parses the given API constants file (must be a valid Python
        script)."""

        try:
            for line in file(path, 'r'):
                exec line in self.constants
            if self.constants['PL_API_PORT'] == 443:
                self.server_url = "https://"
            else:
                self.server_url = "http://"
            self.server_url += self.constants['PL_API_SERVER'] + \
                               ":" + str(self.constants['PL_API_PORT']) + \
                               "/" + self.constants['PL_API_PATH'] + "/"
            return True
        except Exception, err:
            return False

    def begin(self):
        self.multi = True

    def commit(self):
        if self.calls:
            ret = []
            results = self.server.system.multicall(self.calls)
            for result in results:
                if type(result) == type({}):
                    raise Fault(item['faultCode'], item['faultString'])
                elif type(result) == type([]):
                    ret.append(result[0])
                else:
                    raise ValueError, "unexpected type in multicall result"
        else:
            ret = None

        self.calls = []
        self.multi = False

        return ret

    def call(self, method, *params):
        if self.multi:
            self.calls.append({'methodName': method, 'params': list(params)})
            return None
        else:
            return eval("self.server.%s(*params)" % method)

    def make_definitions(self, prefix):
        """Returns a list of code objects that can be executed by the
        exec statement or eval()."""

        # Get a list of available methods from the server
        self.methods = self.server.system.listMethods()

        calls = []
        for method in self.methods:
            calls.append({ 'methodName': "system.methodSignature", 'params': [method] })
        signatures_list = [result[0] for result in self.server.system.multicall(calls)]

        definitions = []

        # Hack for system.* calls
        definitions.append(compile("system = %s" % prefix, prefix, "single"))

        for method, signatures in zip(self.methods, signatures_list):
            if not signatures or min(map(len, signatures)) < 1:
                continue

            for signature in signatures:
                # Pop the return value from the signature
                signature.pop(0)
                # Pop the authentication parameter, too
                if not re.match("system.", method):
                    signature.pop(0)
            
            # Sort signatures by number of arguments
            signatures.sort(lambda x, y: len(x) - len(y))
            
            # Build up function parameters and arguments to call()
            min_args = len(signatures[0])
            max_args = len(signatures[-1])
            params = [arg + str(i) for i, arg in enumerate(signatures[0])]
            params += [arg + str(min_args + i) + "=None" for i, arg in enumerate(signatures[-1][min_args:max_args])]
            args = [arg + str(i) for i, arg in enumerate(signatures[-1])]
            
            # Hack for system.* calls
            if re.match("system.", method):
                function = re.sub("system.", "system_", method)
                auth = ""
            elif re.match("Anon", method):
                function = method
                auth = "%s.anon," % prefix
            else:
                function = method
                auth = "%s.auth," % prefix
            
            definition  = "def %s(%s):" % (function, ",".join(params)) + os.linesep
            for i in range(min_args, max_args):
                definition += "    if %s is None:" % args[i] + os.linesep
                definition += "        return %s.call('%s', %s%s)" % (prefix, method, auth, ",".join(args[0:i])) + os.linesep
            definition += "    return %s.call('%s', %s%s)" % (prefix, method, auth, ",".join(args)) + os.linesep

            if self.verbose:
                print definition

            definitions.append(compile(definition, prefix, "single"))

            # Hack for system.* calls
            if re.match("system.", method):
                definitions.append(compile("%s = %s" % (method, function), prefix, "single"))

        # Also override built-in Python help function
        definitions.append(compile("help = %s.help" % prefix, prefix, "single"))

        return definitions

    def help(self, func):
        """Custom help function for PLCAPI functions."""

        # help(Adm...) or help(Slice...)
        if inspect.isfunction(func) and func.__name__ in self.methods:
            pydoc.pager(self.server.system.methodHelp(func.__name__))
            return

        # help(help)
        if func == self.help:
            func = self.python_help

        # help(...)
        self.python_help(func)

def plcapi(globals, args = sys.argv, shortopts = "", longopts = [], moreusage = ""):
    """Instantiates a connection to a PLCAPI server. Attempts to parse
    command line options and/or an API configuration file. Defines
    functions for all known methods in the specified
    namespace. Returns a tuple of (plcapi, moreopts, argv):

    plcapi - handle to the instantiated connection
    moreopts - dictionary of additional parsed options and their values
    argv - non-option arguments

    globals - namespace in which to define PLCAPI methods
    args - command line argument list
    shortopts - additional short options suitable for passing to getopt.getopt()
    longopts - additional long options suitable for passing to getopt.getopt()
    moreusage - additional usage information to be printed if --help is seen
    """

    __PLCAPI = PLCAPI()

    # Parse command line options and API configuration file
    (moreopts, argv) = __PLCAPI.getopt(args, shortopts, longopts, moreusage)

    # Define functions for all known methods in specified namespace
    globals['__PLCAPI'] = __PLCAPI
    for definition in __PLCAPI.make_definitions('__PLCAPI'):
        exec definition in globals

    return (__PLCAPI, moreopts, argv)

if __name__ == '__main__':
    if len(sys.argv) > 1 and sys.argv[1] in ['build', 'install']:
        from distutils.core import setup
        setup(py_modules=["plcapilib"])
