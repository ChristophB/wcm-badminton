import re
import heapq

from django.shortcuts import render
from django.views import generic
from django.views.generic import TemplateView
from django.views.generic.base import View
from django.http import HttpResponse
from django.db.models import Count
from django_tables2 import RequestConfig, SingleTableView
from django.core import serializers

from analytics.models import Player, PlayerLanguage
from analytics.forms import GroupCountForm
from analytics.tables import PlayerTable
from analytics.filters import PlayerFilter

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
 
        
class ResultView(TemplateView):
    template_name = 'analytics/result.html'

    def get_context_data(self, **kwargs):
        context = super(ResultView, self).get_context_data()
        if self.request.GET.get('group_count'):
            group_count = self.request.GET.get('group_count')
            if group_count in ('nationality'):
                group_count += '__nationality'
                charttype      = "discreteBarChart"
                chartcontainer = 'discretebarchart_container'
            if group_count in ('club', 'coach'):
                charttype      = "discreteBarChart"
                chartcontainer = 'discretebarchart_container'
                group_count += '__name'

            xdata = Player.objects.values_list(group_count).order_by(group_count).distinct()
            ydata = Player.objects.values_list(group_count).annotate(count=Count(group_count)).order_by(group_count).values_list('count') 
            chartdata = {'x': xdata, 'y': ydata}
            
            if group_count in ('hand', 'gender'):
                charttype      = "pieChart"
                chartcontainer = 'piechart_container'
            context['charttype']      = charttype
            context['chartdata']      = chartdata
            context['chartcontainer'] = chartcontainer
            context['extra'] = {
                'x_is_date'      : False,
                'x_axis_format'  : '',
                'tag_script_js'  : True,
                'jquery_on_ready': False,
            }
        return context


class QueryView(TemplateView):
    template_name = 'analytics/query.html'

    def get_context_data(self, **kwargs):
        context = super(QueryView, self).get_context_data()
        context['group_count_form'] = GroupCountForm()
        return context

class IndexView(TemplateView):
    template_name = 'analytics/index.html'
    
class EditorView(TemplateView):
    template_name = 'analytics/editor.html'
    
class InternationalView(TemplateView):
    template_name = 'analytics/international.html'
    
    def get_context_data(self, **kwargs):
        context = super(InternationalView, self).get_context_data()
        group_count = 'nationality__countrycode'
            
        countries = Player.objects.values_list(group_count).order_by(group_count).distinct()
        
        numberOfThingsWhole = Player.objects.values_list(group_count).annotate(count=Count(group_count)).order_by(group_count).values_list('count') 
        
        countryList = list(countries)
        numberList = list(numberOfThingsWhole)
        maxPlayers = max(numberList)
        
        #create top ten countries
        sortedList = list(numberList)
        sortedList.sort(reverse=True)
        helperList = list(numberList)
        counter = 1
        topTen = '<b>Top 10 countries</b> <br> <br>'
        while counter <= 10:
            numberOfPlayers = sortedList[counter - 1]
            maxIndex = helperList.index(numberOfPlayers)
            countryName = countryList[maxIndex]
            topTen += str(counter) + '. ' + countryName[0] + ' ' + str(numberOfPlayers[0]) + '<br>'
            counter = counter + 1
        context['topTen'] = topTen
        
        #create data in json format
        counter = 0;
        data = "{"        
        while counter < len(countries):
            country = countryList[counter]
            numberOfThings = numberList[counter]
            if 0 < numberOfThings[0] < int(maxPlayers[0]/20):
                fillKey = 'LOW'
            elif int(maxPlayers[0]/20) <= numberOfThings[0] < int(maxPlayers[0]/5):
                fillKey = 'MEDIUM' 
            elif int(maxPlayers[0]/5) <= numberOfThings[0] < int(maxPlayers[0]/2):
                fillKey = 'HIGH'
            elif numberOfThings[0] >= int(maxPlayers[0]/2):
                fillKey = 'VERYHIGH' 
            else:
                fillKey = 'defaultFill'
            #create data in json format
            data += str(country[0])
            data += ": { fillKey: '" + fillKey
            data += "', numberOfThings: " + str(numberOfThings[0]) + "},"
              
            counter = counter + 1   
        data += "}"
        context['data'] = data
        
        return context
    