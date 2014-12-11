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


class PlayerView(generic.DetailView):
    model               = Player
    template_name       = 'analytics/player.html'
    context_object_name = 'player'

    # def get_context_data(self, **kwargs):
    #     context = super(PlayerView, self).get_context_data()
    #     template_fields = []
    #     for field in context['player']._meta.fields:
    #         template_fields += [
    #             field.name
    #             , getattr(context['player'], field.name)
    #         ] 
    #     context['fields'] = template_fields
    #     return context
    
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
 
        
