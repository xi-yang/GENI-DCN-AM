### updated by the toplevel Makefile
version_tag="2.0-9"
scm_url="git://git.onelab.eu/sfa.git@sfa-2.0-9"
import socket
 
def version_core (more={}):
    core = { 'code_tag' : version_tag,
             'code_url' : scm_url,
             'hostname' : socket.gethostname(),
             }
    core.update(more)
    return core
