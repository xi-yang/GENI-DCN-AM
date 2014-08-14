### updated by the toplevel Makefile

version_tag="2.0-9"
scm_url="git://git.onelab.eu/sfa.git@sfa-2.0-9"
svn_info="$Revision$ $HeadURL$"
import socket
import re
 
m = re.search('Revision: ([^ ]+) \$ \$HeadURL: ([^ ]+) ', svn_info)

ver='r2.0-v'+m.group(1) 
url=m.group(2) 
indx=url.find('/SFA') 
url=url[:indx]

def version_core (more={}):
    core = { 'code_tag' : ver,
             'code_url' : url,
             'hostname' : socket.gethostname(),
             }
    core.update(more)
    return core
