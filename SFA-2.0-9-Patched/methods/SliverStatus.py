from sfa.util.xrn import urn_to_hrn
from sfa.util.method import Method

from sfa.storage.parameter import Parameter, Mixed

class SliverStatus(Method):
    """
    Get the status of a sliver
    
    @param slice_urn (string) URN of slice to allocate to
    
    """
    interfaces = ['aggregate', 'slicemgr', 'component']
    accepts = [
        Parameter(str, "Slice URN"),
        Mixed(Parameter(str, "Credential string"),
              Parameter(type([str]), "List of credentials")),
        Parameter(dict, "Options")
        ]
    returns = Parameter(dict, "Status details")

    def call(self, slice_xrn, creds, options):
        hrn, type = urn_to_hrn(slice_xrn)
        valid_creds = self.api.auth.checkCredentials(creds, 'sliverstatus', hrn, options) 

        self.api.logger.info("interface: %s\ttarget-hrn: %s\tmethod-name: %s"%(self.api.interface, hrn, self.name))
    
        status = self.api.manager.SliverStatus(self.api, hrn, valid_creds, options=options)

        return status
    
