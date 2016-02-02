from sfa.rspecs.elements.element import Element

class Sliver(Element):
    fields = [
        'sliver_id',
        'component_id',
        'client_id',
        'name',
        'type',
        'tags',
    ]
