from django.conf.urls import patterns, url
from analytics import views


urlpatterns = patterns(
    ''
    #, url(r'^$', views.IndexView.as_view(), name='index')
    , url(r'^(?P<pk>[\dA-Z\-]+)/$'
          , views.PlayerView.as_view()
          , name='player')
    , url(r'^search/$'
          , views.SearchView.as_view()
          , name='search')
)                   
