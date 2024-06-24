package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

import jp.co.metateam.library.service.StockService;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

   Optional<RentalManage> findById(Long id);

    @Query("SELECT COUNT (rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0,1) AND rm.id <> ?2")
    Long otherReservations(String stock_id,Long id);

    @Query("SELECT COUNT (rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0,1) AND rm.id <> ?2 AND ( rm.expectedRentalOn > ?3 OR rm.expectedReturnOn < ?4)")
    Long otherDates(String stock_id,Long id,Date expectedReturnOn,Date expectedRentalOn);

    @Query("SELECT COUNT (rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0,1)")
    Long addOtherReservations(String stock_id);

    @Query("SELECT COUNT (rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0,1) AND ( rm.expectedRentalOn > ?2 OR rm.expectedReturnOn < ?3)")
    Long addOtherDates(String stock_id, Date expectedReturnOn, Date expectedRentalOn);


    //ここから在庫カレンダー
    //ステータスが「貸出待ち」の他の貸出期間と被っている情報
    @Query("SELECT COUNT (rm) FROM RentalManage rm WHERE rm.status = 0 AND (rm.expectedRentalOn <= ?1 AND rm.expectedReturnOn >= ?1) AND rm.stock.id IN ?2")
    Long scheduledRentaWaitData(Date day, List<String>stock_id);

    //ステータスが「貸出中」の他の貸出期間と被っている情報
    @Query(value ="SELECT * FROM rental_manage as rm WHERE CAST(rm.rentaled_At as date) <= :date AND :date <= rm.expected_return_on AND rm.stock_id IN(:stockId) AND rm.status= 1",nativeQuery = true)
    List<RentalManage> scheduledRentalingData(Date date, List<String> stockId);

}
