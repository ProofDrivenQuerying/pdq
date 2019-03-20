create table public.nation (n_nationkey integer, n_name varchar(1024), n_regionkey integer, n_comment varchar(1024))
insert into public.nation (n_nationkey, n_name, n_regionkey, n_comment) values(1, 'USA', 0, 'comment')
create table public.country (c_nationkey integer, c_area integer)
insert into public.country (c_nationkey, c_area) values(1, 1)