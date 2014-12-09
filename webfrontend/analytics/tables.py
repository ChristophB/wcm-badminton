import django_tables2 as tables
from analytics.models import Player

class PlayerTable(tables.Table):
    class Meta:
        model = Player
        attrs = {"class": "paleblue"}
