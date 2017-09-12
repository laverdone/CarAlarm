package security.utils.db;

import android.provider.BaseColumns;
/**CREATE  TABLE IF NOT EXISTS `diary` (
		  `diaryid` INT NOT NULL ,
		  `diaryname` VARCHAR(45) NULL ,
		  `diarydtcreation` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ,
		  PRIMARY KEY (`diaryid`) );
*/
public interface CarAlarmGuardTable extends BaseColumns {
	
	String TABLE_NAME = "guardlog";
	
	String LOGID = "logid";
	
	String LOGDATE = "logdate";
	
	String LOGTYPE = "logtype";
	
	String LOGDESC = "logdesc";

    String LOGLONGITUDE = "longitude";

    String LOGLATIDUTE = "latidute";

    String LOGALTIDUTE = "altidute";
	
	String[] COLUMNS = new String[] {LOGID, LOGDATE, LOGTYPE, LOGDESC};

	
}
