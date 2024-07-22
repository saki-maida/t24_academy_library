package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    public String validateStatus (Integer prevstatus) {

        if(prevstatus == RentalStatus.RENT_WAIT.getValue()&& this.status== RentalStatus.RETURNED.getValue()){
            return "貸出ステータスは貸出待ちから返却済みにできません";
        }else if(prevstatus == RentalStatus.RENTAlING.getValue()&& this.status== RentalStatus.RENT_WAIT.getValue()){
            return "貸出ステータスは貸出中から貸出待ちにできません";
        }else if(prevstatus == RentalStatus.RENTAlING.getValue()&& this.status== RentalStatus.CANCELED.getValue()){
            return "貸出ステータスは貸出中からキャンセルにできません";
        }else if(prevstatus == RentalStatus.CANCELED.getValue()&& this.status== RentalStatus.RENT_WAIT.getValue()){
            return "貸出ステータスはキャンセルから貸出待ちにできません";
        }else if(prevstatus == RentalStatus.CANCELED.getValue()&& this.status== RentalStatus.RENTAlING.getValue()){
            return "貸出ステータスはキャンセルから貸出中にできません";   
        }else if(prevstatus == RentalStatus.CANCELED.getValue()&& this.status== RentalStatus.RETURNED.getValue()){
            return "貸出ステータスはキャンセルから返却済みにできません"; 
        }else if(prevstatus == RentalStatus.RETURNED.getValue()&& this.status== RentalStatus.RENT_WAIT.getValue()){
            return "貸出ステータスは返却済みから貸出待ちにできません";
        }else if(prevstatus == RentalStatus.RETURNED.getValue()&& this.status== RentalStatus.RENTAlING.getValue()){
            return  "貸出ステータスは返却済みから貸出中にできません";   
        }else if(prevstatus == RentalStatus.RETURNED.getValue()&& this.status== RentalStatus.CANCELED.getValue()){
            return "貸出ステータスは返却済みからキャンセルにできません";
        }
        return null;
    }
    public String DateError(RentalManage rentalManage, RentalManageDto rentalManageDto) {
 
        LocalDate nowDate = LocalDate.now(ZoneId.of("Asia/Tokyo"));
     
        Integer prestatus = rentalManage.getStatus();
        Integer poststatus = rentalManageDto.getStatus();
     
        LocalDate expectedRentalOn = rentalManageDto.getExpectedRentalOn().toInstant().atZone(ZoneId.systemDefault())
            .toLocalDate();
     
        LocalDate expectedReturnOn = rentalManageDto.getExpectedReturnOn().toInstant().atZone(ZoneId.systemDefault())
            .toLocalDate();
     
        if (prestatus == 0 && poststatus == 1) {
          if (!expectedRentalOn.equals(nowDate)) {
            return "現在の日付を選択してください";
          }
        }
        if (prestatus == 1 && poststatus == 2) {
          if (!expectedReturnOn.equals(nowDate)) {
            return "現在の日付を選択してください";
          }
        }
        return null;
      }

    public String isReturnDateError(RentalManageDto rentalManageDto){
        Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
        Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();

        if(expectedRentalOn.after(expectedReturnOn)){
            return "返却予定日は貸出予定日より後の日付にしてください";
        }
        return null;
    }
}
