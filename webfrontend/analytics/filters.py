import django_filters
import datetime

from analytics.models import Player, PlayerLanguage, PlayerDiscipline

DISCIPLINE_CHOICES = (
    ('', '---------')
    , (1, 'Men\'s Singles')
    , (2, 'Women\'s Singles')
    , (3, 'Men\'s Doubles')
    , (4, 'Women\'s Doubles')
    , (5, 'Mixed Doubles')

)

class WithinDateFilter(django_filters.DateFilter):
    def filter(self, qs, value):
        if value:
            filter_lookups = {
                "%s__range" % (self.name, ): (
                    value,
                    datetime.datetime.now(),
                ),
            }
            qs = qs.filter(**filter_lookups)
        return qs

class LanguageFilter(django_filters.CharFilter):
    def filter(self, qs, language):
        if language:
            inner_qs = PlayerLanguage.objects.filter(
                language__language__icontains = language
            )
            qs = qs.filter(id__in = inner_qs)
        return qs

class DisciplineFilter(django_filters.ChoiceFilter):
    def filter(self, qs, discipline):
        if discipline:
            inner_qs = PlayerDiscipline.objects.filter(
                discipline__id = discipline
            )
            qs = qs.filter(id__in = inner_qs)
        return qs

class PlayerFilter(django_filters.FilterSet):
    class Meta:
        model = Player

    name              = django_filters.CharFilter(lookup_type='icontains')
    firstname         = django_filters.CharFilter(lookup_type='icontains')
    birthdate         = WithinDateFilter(name='birthdate', label='Birthdate since')
    height            = django_filters.RangeFilter()
    teammember_since  = django_filters.RangeFilter()
    debut_year        = django_filters.RangeFilter()
    start_competitive = django_filters.RangeFilter() 
    language          = LanguageFilter()
    discipline        = DisciplineFilter(choices = DISCIPLINE_CHOICES)

    
