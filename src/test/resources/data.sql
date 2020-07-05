INSERT INTO tl_configuration(default_configuration,active,to_be_enabled,to_be_disabled,green_duration_in_seconds,
red_duration_in_seconds,orange_duration_in_seconds,priority)
VALUES (true,true,false,false,1, 1 , 1, 0);
INSERT INTO tl_configuration(default_configuration,active,to_be_enabled,to_be_disabled,green_duration_in_seconds,
red_duration_in_seconds,orange_duration_in_seconds,priority,start_cron_expression,end_cron_expression)
VALUES (false,true,false,false,2, 2 , 2, 1, '0 * * 1/1 * ?', '42 * * 1/1 * ?');
INSERT INTO tl_configuration(default_configuration,active,to_be_enabled,to_be_disabled,green_duration_in_seconds,
red_duration_in_seconds,orange_duration_in_seconds,priority,start_cron_expression,end_cron_expression)
VALUES (false,true,false,false,5, 5 , 5, 2, '0 * * 1/1 * ?', '45 * * 1/1 * ?');