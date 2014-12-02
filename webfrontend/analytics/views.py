from django.shortcuts import render
from django.views import generic
from analytics.models import Player

class SearchView(generic.ListView):
    model               = Player
    template_name       = 'analytics/search.html'
    context_object_name = 'players'
    paginate_by         = 10

    def get_paginate_by(self, queryset):
        return self.request.GET.get(
            'paginate_by'
            , self.paginate_by
        )
    
    def get_context_data(self, **kwargs):
        context = super(SearchView, self).get_context_data(**kwargs)
        if self.request.GET.get('paginate_by'):
            context['paginate_by'] = int(
                self.request.GET.get('paginate_by')
            )
        context['paginate_by_list'] = (10, 25, 50, 75, 100)
        return context

    def get_queryset(self):
        return Player.objects.order_by('name')

class PlayerView(generic.DetailView):
    model               = Player
    template_name       = 'analytics/player.html'
    context_object_name = 'player'
