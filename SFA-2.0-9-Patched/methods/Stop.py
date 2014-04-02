from sfa.util.xrn import urn_to_hrn
from sfa.util.method import Method

from sfa.trust.credential import Credential
 
from sfa.storage.parameter import Parameter, Mixed

class Stop(Method):
    """
    Stop the specified slice      

    @param cred credential string specifying the rights of the caller
    @param xrn human readable name of slice to instantiate (hrn or urn)
    @return 1 is successful, faults otherwise  
    """

    interfaces = ['aggregate', 'slicemgr', 'component']
    
    accepts = [
        Parameter(str, "Human readable name of slice to instantiate (hrn or urn)"),
        Mixed(Parameter(str, "Credential string"),
              Parameter(type([str]), "List of credentials")),
        Parameter(dict, "options"),
        ]

    returns = Parameter(int, "1 if successful")
    
    def call(self, xrn, creds, options):
        hrn, type = urn_to_hrn(xrn)
        valid_creds = self.api.auth.checkCredentials(creds, 'stopslice', hrn)

        #log the call
        origin_hrn = Credential(string=valid_creds[0]).get_gid_caller().get_hrn()
        self.api.logger.info("interface: %s\tcaller-hrn: %s\ttarget-hrn: %s\tmethod-name: %s"%(self.api.interface, origin_hrn, hrn, self.name))

        self.api.manager.stop_slice(self.api, xrn, creds, options)
 
        return 1 
