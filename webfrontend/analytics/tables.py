import django_tables2 as tables
from analytics.models import Player

class PlayerTable(tables.Table):
    class Meta:
        model  = Player
        attrs  = {'class': 'paleblue'}
        fields = (
            'id', 'name', 'firstname', 'birthdate'
            , 'gender', 'nationality', 'hand'
        )

    id = tables.TemplateColumn(
        '<a href="/analytics/{{ record.id }}">{{ record.id }}</a>'
    )
