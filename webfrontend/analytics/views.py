import re
import sys

from django.core import serializers
from django.core.context_processors import csrf
from django.db import connection
from django.db.models import Count
from django.http import HttpResponse
from django.shortcuts import render, render_to_response, redirect
from django.template import RequestContext
from django.views import generic
from django.views.generic import TemplateView
from django.views.generic.base import View
from django.views.generic.edit import FormView
from django_tables2 import RequestConfig, SingleTableView

from analytics.filters import PlayerFilter
from analytics.forms import GroupCountForm, QueryForm
from analytics.models import Player, PlayerLanguage, Image, PlayerDiscipline
from analytics.tables import PlayerTable


class PlayerView(generic.DetailView):
    model               = Player
    template_name       = 'analytics/player.html'
    context_object_name = 'player'

    def get_context_data(self, **kwargs):
        context = super(PlayerView, self).get_context_data()
        context['image'] = Image.objects.get(
            player_id = self.kwargs.get('pk')
        )
        context['language'] = PlayerLanguage.objects.filter(
            player = self.kwargs.get('pk')
        ).values_list('language__language', flat = True)
        context['discipline'] = PlayerDiscipline.objects.filter(
            player = self.kwargs.get('pk')
        ).values_list('discipline__name', flat = True)

        return context


class PlayerUpdateView(generic.edit.UpdateView):
    model = Player
    fields = [
        'hand', 'gender', 'name', 'firstname'
        , 'height', 'coach', 'nationality', 'club'
    ]
    template_name = 'analytics/player_update.html'

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
            
            context['groupcount'] = group_count            
            if group_count in ('nationality'): 
                return redirect('international')
            if group_count in ('club', 'coach'):
                charttype      = 'discreteBarChart'
                chartcontainer = 'discretebarchart_container'
                group_count   += '__name'
            if group_count in ('height'):
                charttype      = 'discreteBarChart'
                chartcontainer = 'discretebarchart_container'
            if group_count in ('language', 'discipline'):
                charttype      = 'discreteBarChart'
                chartcontainer = 'discretebarchart_container'
                if group_count == 'language':
                    group_count += '__language'
                    xdata = PlayerLanguage.objects.values_list(group_count).order_by(group_count).distinct()
                    ydata = PlayerLanguage.objects.values_list(group_count).annotate(count=Count(group_count)).order_by(group_count).values_list('count')
                else:
                    group_count += '__name'
                    xdata = PlayerDiscipline.objects.values_list(group_count).order_by(group_count).distinct()
                    ydata = PlayerDiscipline.objects.values_list(group_count).annotate(count=Count(group_count)).order_by(group_count).values_list('count')
                    group_count = 'discipline'
            else:
                xdata = Player.objects.values_list(group_count).order_by(group_count).distinct()
                ydata = Player.objects.values_list(group_count).annotate(count=Count(group_count)).order_by(group_count).values_list('count')

            xdata = makeXDataReadable(xdata)

            if group_count in ('hand', 'gender', 'discipline'):
                charttype      = "pieChart"
                chartcontainer = 'piechart_container'
                sum = 0
                sumKnown = 0
                count = 0
                unknownIndex = getIndexForUnknownData(xdata)
                for absoluteValue in ydata:
                    sum += int(absoluteValue[0])
                    if unknownIndex != -1 and count != unknownIndex:
                        sumKnown += int(absoluteValue[0])
                    count = count + 1

                relValueList = list()
                relValueKnownList = list()
                count = 0
                for absoluteValue in ydata:
                    relValue = round(absoluteValue[0] / sum * 100, 2)
                    relValueList.append(relValue)
                    if unknownIndex != -1 and count != unknownIndex:
                        relValue = round(absoluteValue[0] / sumKnown * 100, 2)
                        relValueKnownList.append(relValue)
                    count = count + 1
                     
                context['relativeValues'] = relValueList
                context['relativeValuesKnownData'] = relValueKnownList
            
            if group_count in ('discipline'):
                context['fiveResults'] = 'yes'

            chartdata = {'x': xdata, 'y': ydata}

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

def getIndexForUnknownData( xdata ):
    index = 0;
    for value in xdata:
        if str(value[0]) == 'unknown':
            return index
        else:
            index = index + 1 
    return -1;

def makeXDataReadable(xdata):
    newXData = list()
    for value in xdata:
        if str(value[0]) == 'u':
            newXData.append('unknown')
        elif str(value[0]) == 'm':
            newXData.append('male')
        elif str(value[0]) == 'f':
            newXData.append('female')
    if not newXData: 
        return xdata
    return newXData;

class QueryView(TemplateView):
    template_name = 'analytics/query.html'

    def get_context_data(self, **kwargs):
        context = super(QueryView, self).get_context_data()
        context['group_count_form'] = GroupCountForm()
        return context

class IndexView(TemplateView):
    template_name = 'analytics/index.html'
    
class EditorView(FormView):
    template_name = 'analytics/editor.html'
    form_class    = QueryForm
    success_url   = '.'

    def form_valid(self, form):
        c = {}
        header = []
        message = ''
        data = []
        c.update(csrf(self.request))
        form  = QueryForm(initial=self.request.POST) 
        query = self.request.POST.get('query')
        
        if ('insert' in query.lower() or 'update' in query.lower() 
            or 'delete' in query.lower() or 'create' in query.lower() 
            or 'drop' in query.lower()):
            message = 'Only SELECT is allowed!'
        else:
            cursor = connection.cursor()
            try:
                cursor.execute(query)
                desc   = cursor.description
                header = [col[0] for col in desc] 
                data   = cursor.fetchall()
            except:
                message = sys.exc_info()[1]
            finally:
                cursor.close()

        return render_to_response(
            'analytics/editor.html'
            , {
                'form'     : form 
                , 'header' : header
                , 'data'   : data
                , 'message': message
            }
            , RequestContext(self.request)
        )
        
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
    
    def InternationalView(request):
        return redirect('analytics/international.html')
