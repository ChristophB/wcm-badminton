from django.conf.urls import patterns, url
from django.views.generic import RedirectView

from analytics import views


urlpatterns = patterns(
    ''
    , url(r'^$', views.IndexView.as_view(), name='index')
    , url(r'^(?P<pk>[\dA-Z\-]+)/$'
          , views.PlayerView.as_view()
          , name='player')
    , url(r'^player_update/(?P<pk>[\dA-Z\-]+)/$'
          , views.PlayerUpdateView.as_view()
          , name='player_update')
    , url(r'^search/$'
          , views.SearchView.as_view()
          , name='search')
    , url(r'^query/$'
          , views.QueryView.as_view()
          , name='query')
    , url(r'^result/$'
          , views.ResultView.as_view()
          , name='result')
    , url(r'^result/international/$'
          , views.InternationalView.as_view()
          , name='international')               
    , url(r'^editor/$'
          , views.EditorView.as_view()
          , name='editor')
    , url(r'^analytics/result/?csrfmiddlewaretoken=UHB3f9Ep70gglnXMeWLUW1EShVvXHOpF&group_count=nationality&submit_group_count=Show/$', RedirectView.as_view(url='/result/international/'))
)                   
