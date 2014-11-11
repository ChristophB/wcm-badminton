#!usr/bin/perl
use strict;
use DBI;
use Carp;
use DBI;
use constant {
    USER => 'postgres',
    PASS => 'password',
    DB   => 'dbi:Pg:dbname=testdb;host=localhost'
};

my $dbh = DBI->connect(
	DB, USER, PASS
	, {AutoCommit => 0, RaiseError => 1, PrintError => 0}
	) or croak('Error: DB-Connection failed!');

sub insertData {
    my $data = shift or croak('Error: Parameter %data missing!');
    my $id = getId('players', ['id'], [$data->{id}]);
    
    return if ($id);
    $data->{club} = 
	insertReferencedValue('clubs', ['name'], [$data->{club}]);
    $data->{coach} = 
	insertReferencedValue('coaches', ['name'], [$data->{coach}]);
    $data->{nationality} = 
	insertReferencedValue('nationalities', ['nationality'], [$data->{nationality}]);
    $data->{style} =
	insertReferencedValue('styles', ['style'], [$data->{style}]);
    $data->{birthplace_city} = 
	insertReferencedValue('cities', ['name', 'state']
			      , [@{$data}{('birthplace_city', 'birthplace_state')}]);
    $data->{cur_residence_city} = 
	insertReferencedValue('cities', ['name', 'state']
			     , [@{$data}{('cur_residence_city', 'cur_residence_state')}]);

    insertRow(
	'players'
	, undef
	, [@{$data}{(
	       'id', 'firstname', 'name', 'birthdate'
	       , 'gender', 'birthplace_city', 'club', 'coach'
	       , 'cur_residence_city', 'debut_year', 'hand', 'height'
	       , 'nationality', 'nickname', 'start_competitive', 'style'
	       , 'teammember_since'
	    )}]
	);
    
    foreach (@{$data->{languages}}) {
	$_ = insertReferencedValue('languages', ['language'], [$_]);
	insertRow('player_language', undef, [$data->{id}, $_]);
    }

    insertRow('images', undef, [@{$data}{('id', 'image')}]);
    insertRow('webresources', undef, [@{$data}{('id', 'facebook', 'twitter', 'website')}]);

    return 1;
}

sub getId {
    my $table   = shift or croak('Error: Parameter $table missing!');
    my $columns = shift or croak('Error: Parameter $columns missing!');
    my $values  = shift or croak('Error: Parameter $values missing!');
    my $id      = $dbh->selectrow_array(
	'SELECT DISTINCT id'. "\n"
	. 'FROM '. $table. "\n"
	. 'WHERE '. join("\n AND ", map($_. ' = ?', @$columns)). "\n"
	. 'LIMIT 1'
	, undef
	, @{$values}
	);
    
    return $id;
}

sub insertReferencedValue {
    my $table   = shift or croak('Error: Parameter $table missing!');
    my $columns = shift or croak('Error: Parameter $columns missing!');
    my $values  = shift or return;
    my $id;
    
    return unless ($values->[0]);
    $id = getId($table, $columns, $values);
    return insertRow($table, $columns, $values) if (!$id);
    return $id if ($id);
}

sub insertRow {
    my $table   = shift or croak('Error: Parameter $table missing!');
    my $columns = shift; 
    my $values  = shift or croak('Error: Parameter $values missing!');
    
    if ($columns) {
	$dbh->do(
	    'INSERT INTO '. $table
	    . ' ('. join(', ', @$columns). ')'. "\n"
	    . 'VALUES ('. ('?, ' x (@$columns - 1)). '?)'
	    , undef
	    , @{$values}
	    ) or croak($dbh->errstr);
	return $dbh->last_insert_id(undef, undef, $table, 'id');
    } else {
	$dbh->do(
	    'INSERT INTO '. $table. "\n"
	    . 'VALUES ('. ('?, ' x (@$values - 1)). '?)'
	    , undef
	    , @{$values}
	    ) or croak($dbh->errstr);
    }
}

sub finishTransaction {
    $dbh->commit;
    $dbh->disconnect;
}

1;
