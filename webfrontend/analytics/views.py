from django.shortcuts import render
from django.views import generic
from django.http import HttpResponse
from django.db.models import Q

from analytics.models import Player, PlayerLanguage
from analytics.forms import SearchForm

# def get_Name(request):
#     if request.method == 'POST':
#         form = SearchForm(request.POST)
#         if form.is_valid():
#             return HttpResponseRedirect('/search/')
#     else:
#         form = SearchForm()
#     return render(request, 'search.html', {'form':form})

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
        context['searchForm'] = SearchForm(data=self.request.GET)
        if self.request.GET.get('paginate_by'):
            context['paginate_by'] = int(
                self.request.GET.get(
                    'paginate_by'
                    , self.paginate_by
                )
            )
        else:
            context['paginate_by'] = 10
        context['paginate_by_list'] = (10, 25, 50, 75, 100)
        return context

    def get_queryset(self):
        result = Player.objects.all()
        if self.request.GET.get('searchSubmit'):
            form = SearchForm(data=self.request.GET)

            if not form.is_valid():
                return result

            name_pattern    = self.request.GET.get('name_pattern')
            birthdate_start = self.request.GET.get('birthdate_start')
            birthdate_to    = self.request.GET.get('birthdate_to')
            club_pattern    = self.request.GET.get('club_pattern')
            nat_pattern     = self.request.GET.get('nat_pattern')
            nat_choice      = self.request.GET.get('nationality')
            hand_choice     = self.request.GET.get('hand')
            style_choice    = self.request.GET.get('style')
            language_choice = self.request.GET.get('language')

            if name_pattern:
                result = result.filter(
                    Q(name__contains=name_pattern)
                    | Q(firstname__contains=name_pattern)
                )
            if birthdate_start:
                if not birthdate_to:
                    result = result.filter(birthdate=birthdate_start)
                else:
                    result = result.filter(
                        birthdate__range=(
                            birthdate_start
                            , birthdate_to
                        )
                    )
            if club_pattern:
                result = result.filter(
                    club__name__contains=club_pattern
                )
            if nat_choice:
                result = result.filter(nationality__id=nat_choice)
            elif nat_pattern:
                result = result.filter(
                    nationality__nationality__contains=nat_pattern
                )
            if hand_choice:
                result = result.filter(hand=hand_choice)
            if style_choice:
                result = result.filter(style__id=style_choice)
            if language_choice: ## TODO: Check
                result = result.filter(
                    id__in=PlayerLanguage.objects.filter(
                        language=language_choice
                    ).values_list('player', flat=True)
                )
        return result

class PlayerView(generic.DetailView):
    model               = Player
    template_name       = 'analytics/player.html'
    context_object_name = 'player'
