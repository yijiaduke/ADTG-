# Database

## Branching Policy

Please use a new brach when needed to modify the database structure and submit a merge request. Assign Javier Pastorino as reviewer/approver of the merge request.


## Database information

We choose to use a rational database - POSTGRESQL so that we can use SQL language to select data with the DAO functions.

### Installation 

We install the database to Duke VM so that everyone can access to it, see Connection to Database for further information. 

We create an account named 'postgre', a database named 'proj24db', with a password '12345'

command to install: 

sudo apt install postgresql postgresql-contrib

sudo systemctl start postgresql.service

sudo -i -u postgres 

psql

CREATE DATABASE proj24db;

ALTER USER postgres WITH ENCRYPTED PASSWORD '12345';


## Connection to Database

### Requirement

You should connect to a Duke domain network, or connect to a Duke VPN.

### Setup 

1. Modify the PostgreSQL configuration file
2. Modify the pg_hba.conf file
3. Allow port 5432 through the firewall
4. Restart PostgreSQL

See Ref 2 for detailed instructions.

### Command in terminal to connect

psql -h vcm-41568.vm.duke.edu -U postgres -d proj24db

### Connection in Project 

Everything will be manipulated in the 'DAOConn' Class in 'core' repository. Just use the default constructor of DAOConn to get Connection and use specific DAO classes to send requests if you meet the requirement. 

## Reference

1. https://www.digitalocean.com/community/tutorials/how-to-install-postgresql-on-ubuntu-20-04-quickstart

2. https://blog.devart.com/configure-postgresql-to-allow-remote-connection.html
