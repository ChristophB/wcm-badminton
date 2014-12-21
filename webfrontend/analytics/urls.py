from django.conf.urls import patterns, url
from analytics import views


urlpatterns = patterns(
    ''
    , url(r'^$', views.IndexView.as_view(), name='index')
    , url(r'^(?P<pk>[\dA-Z\-]+)/$'
          , views.PlayerView.as_view()
          , name='player')
    , url(r'^search/$'
          , views.SearchView.as_view()
          , name='search')
    , url(r'^query/$'
          , views.QueryView.as_view()
          , name='query')
    , url(r'^result/$'
          , views.ResultView.as_view()
          , name='result')
    , url(r'^editor/$'
          , views.EditorView.as_view()
          , name='editor')
    , url(r'^international/$'
          , views.InternationalView.as_view()
          , name='international')
)                   
