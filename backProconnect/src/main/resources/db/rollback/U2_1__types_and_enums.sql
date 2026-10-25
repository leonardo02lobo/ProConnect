-- Rollback MANUAL de V2.1 (ver docs/db/migrations.md). La normalización de valores no se revierte.
ALTER TABLE friends DROP CHECK ck_friends_status, MODIFY status varchar(255) NOT NULL;
ALTER TABLE `user` DROP CHECK ck_user_role, MODIFY role varchar(255) NOT NULL;
ALTER TABLE postulations DROP CHECK ck_postulations_status;
ALTER TABLE posts CHANGE created_at `date` datetime(6) NULL;
ALTER TABLE comments CHANGE created_at `date` datetime(6) NULL;
ALTER TABLE likes CHANGE created_at `date` datetime(6) NULL;
ALTER TABLE jobs CHANGE created_at `date` datetime(6) NULL;
ALTER TABLE postulations CHANGE created_at `date` datetime(6) NULL;
ALTER TABLE messages CHANGE sent_at `timestamp` datetime(6) NULL;
