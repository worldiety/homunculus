-- MySQL Workbench Forward Engineering



-- -----------------------------------------------------
-- Schema supportiety
-- -----------------------------------------------------
-- DROP SCHEMA IF EXISTS `supportiety` ; <- this does not work with flyway because of the table locks and the schema-table

-- -----------------------------------------------------
-- Schema supportiety
-- -----------------------------------------------------
-- CREATE SCHEMA IF NOT EXISTS `supportiety` DEFAULT CHARACTER SET utf8 ;

-- -----------------------------------------------------
-- Table `supportiety`.`file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(4096) NOT NULL COMMENT 'files are not stored in the db, because mysql is not capable of handling that. The ptr may refer to something external e.g. S3 storage or else. There is no need of deduplication, because log-files and jsons are usually entirely unique.',
  `size` BIGINT NOT NULL,
  `sha2` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `supportiety`.`company`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `company` (
  `id` VARCHAR(255) NOT NULL,
  `title_name` VARCHAR(255) NOT NULL,
  `title_color` VARCHAR(12) NOT NULL,
  `title_logo` BIGINT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_company_logo_idx` (`title_logo` ASC),
  CONSTRAINT `fk_company_logo`
    FOREIGN KEY (`title_logo`)
    REFERENCES `file` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



insert into company(id,title_name,title_color) values ('de.worldiety','worldiety','#2A8E26');