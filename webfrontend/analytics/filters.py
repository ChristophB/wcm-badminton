import django_filters

from analytics.models import Player

class PlayerFilter(django_filters.FilterSet):
    class Meta:
        model = Player

    name      = django_filters.CharFilter(lookup_type='icontains')
    firstname = django_filters.CharFilter(lookup_type='icontains')
    birthdate = django_filters.DateRangeFilter()

    height    = django_filters.RangeFilter()
    
    
