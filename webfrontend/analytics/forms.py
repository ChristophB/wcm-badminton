from django import forms

class GroupCountForm(forms.Form):
    group_count = forms.ChoiceField(
        choices = (
            ('hand', 'Hand')
            , ('club', 'Club')
            , ('coach', 'Coach')
            , ('discipline', 'Discipline')
            , ('gender', 'Gender')
            , ('language', 'Language')
            , ('nationality', 'Nationality')
            , ('height', 'Height')
        )
        , required = True
    )

class QueryForm(forms.Form):
    query = forms.CharField(
        max_length       = 1000
        , required       = True
        , widget         = forms.Textarea(attrs={'cols': 100, 'rows': 10})
        , error_messages = {'required': 'Please enter a SQL query.'}
    )
