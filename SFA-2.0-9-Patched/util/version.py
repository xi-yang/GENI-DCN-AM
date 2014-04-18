### updated by the toplevel Makefile
version_tag="2.0-9"
scm_url="git://git.onelab.eu/sfa.git@sfa-2.0-9"
svn_info="$Revision: 503 $ $HeadURL: https://svn.maxgigapop.net/svn/geni-aggregate/branches/r2.0 $"
import socket
import re
 
m = re.search('Revision: ([^ ]+) \$ \$HeadURL: ([^ ]+) ', svn_info)
 
def version_core (more={}):
    core = { 'code_tag' : 'r2.0-'+m.group(1),
             'code_url' : m.group(2),
             'hostname' : socket.gethostname(),
             }
    core.update(more)
    return core
