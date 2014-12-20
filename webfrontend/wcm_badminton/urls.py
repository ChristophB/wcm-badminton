from django.conf.urls import patterns, include, url
from django.contrib import admin

urlpatterns = patterns(
    ''
    , url(r'^analytics/', include('analytics.urls', namespace='analytics'))
    , url(r'^admin/', include(admin.site.urls))
	, url(r'^', include('analytics.urls'))
)
