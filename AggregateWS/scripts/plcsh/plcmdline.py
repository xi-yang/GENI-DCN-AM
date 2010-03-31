#!/usr/bin/python2

import getpass
import getopt
import os, sys
import xmlrpclib
import readline
import atexit
import string
import xml.parsers.expat


USAGE = """
Usage:
%s -v -u <user> -r <role> [-p <password>] [-c <single_command>]
""" % sys.argv[0]

PROMPT = "PL> "

HISTORY_FILE_NAME = ".plcmdline_history"

XMLRPC_SERVER = 'https://www.planet-lab.org/PLCAPI/'

STANDARD_API_PREFIX = ('Adm', 'Slice', 'Boot', 'AnonAdm', 'AnonSlice')


def is_exit_line(line):
    if line in ('q','quit','exit'):
        return 1
    else:
        return 0


def is_api_line(line):
    if len(line) < 5:
        return 0

    if line[:4] == "api.":
        return 1
    
    return 0



def is_federation_function(line):
    """
    identify whether or not this call is to a federation function.
    this is done by identifying the non-federation calls that start
    with STANDARD_API_PREFIX. this function assumes that the line
    is a api call line, starting with api. (check with is_api_line(..))
    """

    function_name= line[4:]
    federation_function= 1
    
    for prefix in STANDARD_API_PREFIX:
        prefix_len= len(prefix)
        
        if function_name[:prefix_len] == prefix:
            federation_function= 0
            break

    return federation_function


    

def tab_completion_func(text, state):
    return "test"


def main():
    user= None
    password= None
    role= None

    verbose= 0
    single_command= None

    (opts, argv) = getopt.getopt(sys.argv[1:], "u:p:r:v:c:")
    for (opt, optval) in opts:
        if opt == '-u':
            user = optval
        elif opt == '-p':
            password = optval
        elif opt == '-r':
            role = optval
        elif opt == '-v':
            verbose= 1
        elif opt == '-c':
            single_command= optval
                
    if not role or role != "anonymous" and not user:
        print( USAGE )
        sys.exit(1)

    if role != "anonymous" and not password:
        try:
            password= getpass.getpass()
        except (EOFError,KeyboardInterrupt):
            print( "" )
            sys.exit(0)


    # setup environment for commandline
    env_globals= {}
    env_globals['__builtins__']= globals()['__builtins__']

    auth= {}
    if role == "anonymous":
        auth['AuthMethod'] = "anonymous"
    else:
        auth['AuthMethod'] = "password"
        auth['Username'] = user
        auth['AuthString'] = password
        auth['Role'] = role

    env_locals= {}
    env_locals['api_auth']= auth
    env_locals['api']= xmlrpclib.Server(XMLRPC_SERVER,verbose=verbose)

    # incase the user wants to call a federation function, get a session
    # by using the AuthenticatePrincipal function
    federation_auth= {}
    federation_auth['email']= user
    federation_auth['password']= password
    federation_auth['role']= role
    env_locals['federation_auth']= federation_auth
    
    try:
        fed_session= env_locals['api'].AuthenticatePrincipal(federation_auth)
    except xmlrpclib.Fault, fault:
        print( "Could not authenticate with federation API. Fault: ", fault )
        sys.exit(1)
    except xmlrpclib.ProtocolError, err:
        print( "xml rpc protocol error", err )
        sys.exit(1)
    except xml.parsers.expat.ExpatError, err:
        print( "xml parsing error: %s " % str(err) )
        sys.exit(1)

    session_auth= {}
    session_auth['email']= user
    session_auth['session']= fed_session

    env_locals['session_auth']= session_auth
    

    # run a single command and exit if -c was used. we really should
    # have all these exceptions in only one place, which would mean
    # a function used to make api calls. then this code and the interactive
    # code could use it
    if single_command is not None:
        try:
            if is_federation_function("api.%s" % single_command ):
                auth_var= "session_auth"
            else:
                auth_var= "api_auth"
            
            command= "api.%s" % \
                     string.replace( single_command, "(", "(%s," % auth_var )
            
            print "Running %s" % command
            return_val= eval(command,env_globals,env_locals)
            str_return_val= string.replace(repr(return_val),",",",\n")
            print( str_return_val )

            env_locals['api'].InvalidateSession(session_auth)
        except SyntaxError:
            print( "Invalid syntax: %s" % command )
        except xmlrpclib.Fault, fault:
            print( "Fault: ", fault )
        except xmlrpclib.ProtocolError, err:
            print( "xml rpc protocol error", err )
        except xml.parsers.expat.ExpatError, err:
            print( "xml parsing error: %s " % str(err) )
        except Exception, err:
            print( "unhandled exception: %s" % str(err) ) 
        sys.exit(0)


    # no single command, go interactive; setup readline
    history_file = os.path.join(os.environ["HOME"], HISTORY_FILE_NAME)
    try:
        readline.read_history_file(history_file)
    except IOError:
        pass

    atexit.register(readline.write_history_file, history_file)

    try:
        running= 1
        while running:
            try:
                line= raw_input(PROMPT)
            except KeyboardInterrupt:
                print ""
                continue

            line= string.strip(line)
            if line == "":
                continue
            
            if is_exit_line(line):
                running= 0

            try:
                if is_api_line(line):
                    if is_federation_function(line):
                        auth_var= "session_auth"
                    else:
                        auth_var= "api_auth"
                        
                    command= string.replace( line, "(", "(%s," % auth_var )
                    return_val= eval(command,env_globals,env_locals)
                    env_locals['_last']= return_val
                    str_return_val= string.replace(repr(return_val),",",",\n")
                    print( str_return_val )
                else:
                    command= line
                    exec command in env_globals,env_locals                    
            except SyntaxError:
                print( "Invalid syntax: %s" % command )
            except xmlrpclib.Fault, fault:
                print( "Fault: ", fault )
            except xmlrpclib.ProtocolError, err:
                print( "xml rpc protocol error", err )
            except xml.parsers.expat.ExpatError, err:
                print( "xml parsing error: %s " % str(err) )
            except Exception, err:
                print( "unhandled exception: %s" % str(err) )
    except EOFError:
        print( "" )
        pass


    env_locals['api'].InvalidateSession(session_auth)


    

    
if __name__ == '__main__':
    main()
