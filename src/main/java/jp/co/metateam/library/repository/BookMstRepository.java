package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.service.StockService;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface BookMstRepository extends JpaRepository<BookMst, Long> {
	List<BookMst> findAll();

	Optional<BookMst> findById(BigInteger id);

	//書籍情報を取得
	@Query("SELECT bm FROM BookMst bm WHERE bm.deletedAt IS NULL")
	List<BookMst> findAllBookData();

	//書籍タイトルから書籍情報を取得
	@Query("SELECT bm FROM BookMst bm WHERE bm.title = ?1 AND bm.deletedAt IS NULL")
	List<BookMst> findByBookTitle(String title);
}
