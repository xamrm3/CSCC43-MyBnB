use MyBnB;

Drop tables if exists listing, amenities, has, calendar, availability, user, renter, host, rented, owns, rating, ratings;
create table listing(
Lid int not null auto_increment,
type varchar(30) not null,
address varchar(30) not null,
country varchar(30) not null,
city varchar(30) not null,
pc varchar(6) not null,
lat real not null,
longi real not null,
primary key(Lid));

create table amenities(
Aid int not null auto_increment,
amenity varchar(30) not null,
description varchar(250),
primary key(Aid));

create table has(
Aid int not null,
Lid int not null,
foreign key(Aid) references amenities(Aid) on delete cascade,
foreign key(Lid) references listing(Lid) on delete cascade,
primary key(Aid, Lid));

create table calendar(
Crid int not null auto_increment,
start int not null,
end int not null,
price real not null,
booked boolean default false not null,
check(price >= 0),
primary key(Crid));

create table availability(
Lid int not null,
Crid int not null,
foreign key(Lid) references listing(Lid) on delete cascade,
foreign key(Crid) references calendar(Crid) on delete cascade,
primary key(Lid, Crid));

create table user(
Sin int not null auto_increment,
name varchar(30) not null,
age int not null,
job varchar(30) not null,
address varchar(30) not null,
cancels int default 0,
check(age >= 18),
primary key(Sin));

create table renter(
Sin int not null,
credit int not null,
foreign key(Sin) references user(Sin) on delete cascade,
primary key(Sin));

create table host(
Sin int not null,
foreign key(Sin) references user(Sin) on delete cascade,
primary key(Sin));

create table rented(
Lid int not null,
Sin int not null,
Crid int not null,
comment varchar(250),
stars int,
check (stars >= 1 and stars <= 5),
foreign key(Sin) references renter(Sin) on delete cascade,
foreign key(Lid) references listing(Lid) on delete cascade,
foreign key(Crid) references calendar(Crid) on delete cascade,
primary key(Lid, Sin));

create table owns(
Lid int not null,
Sin int not null,
foreign key(Sin) references host(Sin) on delete cascade,
foreign key(Lid) references listing(Lid) on delete cascade,
primary key(Lid, Sin));

create table rating (
Rid int not null auto_increment,
comment varchar(250) not null,
rating int not null,
check (rating >= 1 and rating <= 5),
primary key(Rid)); 

create table ratings (
Sin int not null,
Rid int not null,
foreign key(Sin) references user(Sin) on delete cascade,
foreign key(Rid) references rating(Rid) on delete cascade,
primary key(Sin, Rid));