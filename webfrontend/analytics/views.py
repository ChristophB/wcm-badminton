import re
#import django_filters

from django.shortcuts import render
from django.views import generic
from django.views.generic.base import View
from django.http import HttpResponse
from django.db.models import Q, FieldDoesNotExist
from django_tables2 import RequestConfig, SingleTableView

from analytics.models import Player, PlayerLanguage
from analytics.forms import SearchForm
from analytics.tables import PlayerTable
from analytics.filters import PlayerFilter

# class SearchOldView(generic.ListView):
#     model               = Player
#     template_name       = 'analytics/search.html'
#     context_object_name = 'players'
#     paginate_by         = 10


#     def get_paginate_by(self, queryset):
#         return self.request.GET.get(
#             'paginate_by'
#             , self.paginate_by
#         )

#     def get_context_data(self, **kwargs):
#         context = super(SearchView, self).get_context_data(**kwargs)
#         context['searchForm'] = SearchForm(data=self.request.GET)
#         if self.request.GET.get('paginate_by'):
#             context['paginate_by'] = int(
#                 self.request.GET.get(
#                     'paginate_by'
#                     , self.paginate_by
#                 )
#             )
#         else:
#             context['paginate_by'] = 10
#         context['paginate_by_list'] = (10, 25, 50, 75, 100)
#         context['title_list'] = ('Name', 'Firstname', 'Hand', 'Birthdate')
#         #cur_url = self.request.build_absolute_uri
#         #context['cur_url'] = re.sub('order_by=*?[&$]', 'b', cur_url)
#         return context

#     def get_queryset(self):
#         result   = Player.objects.all()
#         order_by = self.request.GET.get('order_by', 'name')
 
#         if self.request.GET.get('searchSubmit'):
#             form = SearchForm(data=self.request.GET)

#             if not form.is_valid():
#                 return result

#             name_pattern    = self.request.GET.get('name_pattern')
#             birthdate_start = self.request.GET.get('birthdate_start')
#             birthdate_to    = self.request.GET.get('birthdate_to')
#             club_pattern    = self.request.GET.get('club_pattern')
#             nat_pattern     = self.request.GET.get('nat_pattern')
#             nat_choice      = self.request.GET.get('nationality')
#             hand_choice     = self.request.GET.get('hand')
#             style_choice    = self.request.GET.get('style')
#             language_choice = self.request.GET.get('language')

#             if name_pattern:
#                 result = result.filter(
#                     Q(name__contains=name_pattern)
#                     | Q(firstname__contains=name_pattern)
#                 )
#             if birthdate_start:
#                 if not birthdate_to:
#                     result = result.filter(birthdate=birthdate_start)
#                 else:
#                     result = result.filter(
#                         birthdate__range=(
#                             birthdate_start
#                             , birthdate_to
#                         )
#                     )
#             if club_pattern:
#                 result = result.filter(
#                     club__name__contains=club_pattern
#                 )
#             if nat_choice:
#                 result = result.filter(nationality__id=nat_choice)
#             elif nat_pattern:
#                 result = result.filter(
#                     nationality__nationality__contains=nat_pattern
#                 )
#             if hand_choice:
#                 result = result.filter(hand=hand_choice)
#             if style_choice:
#                 result = result.filter(style__id=style_choice)
#             if language_choice: ## TODO: Check
#                 result = result.filter(
#                     id__in=PlayerLanguage.objects.filter(
#                         language=language_choice
#                     ).values_list('player', flat=True)
#                 )
#         try: 
#             field_name = re.sub('^-', '', order_by)
#             Player._meta.get_field_by_name(field_name)
#         except FieldDoesNotExist: 
#             order_by = 'name'
#         result = result.order_by(order_by)
#         return result

class PlayerView(generic.DetailView):
    model               = Player
    template_name       = 'analytics/player.html'
    context_object_name = 'player'

    
class SearchView(SingleTableView):
    model = Player
    table_class = PlayerTable
    filter_class = PlayerFilter
    formhelper_class = None ###
    paginate_by = 25
    context_filter_name = 'filter'
    template_name = 'analytics/search.html'
    
    def get_context_data(self, **kwargs):
        context = super(SearchView, self).get_context_data()
        context[self.context_filter_name] = self.filter
        return context
        
    def get_table(self, **kwargs):
        table = super(SearchView, self).get_table()
        RequestConfig(
            self.request
            , paginate={'per_page': self.paginate_by}
        ).configure(table)
        return table

    def get_queryset(self, **kwargs):
        qs = super(SearchView, self).get_queryset()
        self.filter = self.filter_class(self.request.GET, queryset=qs)
        #self.filter.form.helper = self.formhelper_class()
        return self.filter.qs
 
        
