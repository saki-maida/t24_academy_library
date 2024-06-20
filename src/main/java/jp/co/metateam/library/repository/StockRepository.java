package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);

// 利用可能な総在庫数を取得
@Query("SELECT s FROM Stock s WHERE s.status = 0 AND s.bookMst.id = ?1 AND s.deletedAt IS NULL")
List<Stock> findAllAvailableStockData(Long bookId);

 
// @Query("SELECT DISTINCT s FROM Stock s LEFT OUTER JOIN RentalManage rm ON s.id = rm.stock.id WHERE ?1 BETWEEN rm.expectedRentalOn AND rm.expectedReturnOn AND s.bookMst.id = ?2 AND s.status = 0 AND deletedAt IS null")
//   List<Stock> lendableBook(Date choiceDate, Long id);

// @Query("SELECT s FROM Stock s WHERE s.status = 0 AND s.bookMst.id = ?1 " + "AND deletedAt IS null")
//   List<Stock> bookStockAvailable(Long id);

}
