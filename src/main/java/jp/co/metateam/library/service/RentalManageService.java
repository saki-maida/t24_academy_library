package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;

import jp.co.metateam.library.values.RentalStatus;

@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;
    private final BookMstRepository bookMstRepository;

    @Autowired
    public RentalManageService(
            AccountRepository accountRepository,
            RentalManageRepository rentalManageRepository,
            StockRepository stockRepository,
            BookMstRepository bookMstRepository) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
        this.bookMstRepository = bookMstRepository;
    }

    @Transactional
    public List<RentalManage> findAll() {
        List<RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<RentalManage> findByStockIdAndStatus(String newStockId) {
        return this.findByStockIdAndStatus(newStockId);
    }

    @Transactional
    public Long otherReservations(String stock_id, Long id) {
        return this.rentalManageRepository.otherReservations(stock_id, id);
    }

    @Transactional
    public Long otherDates(String stock_id, Long id, Date expectedReturnOn, Date expectedRentalOn) {
        return this.rentalManageRepository.otherDates(stock_id, id, expectedReturnOn, expectedRentalOn);
    }

    @Transactional
    public long addOtherReservations(String stock_id) {
        return this.rentalManageRepository.addOtherReservations(stock_id);
    }

    @Transactional
    public long addOtherDates(String stock_id, Date expectedReturnOn, Date expectedRentalOn) {
        return this.rentalManageRepository.addOtherDates(stock_id, expectedReturnOn, expectedRentalOn);
    }

    @Transactional
    public List<String> findLendableBook(Date choiceDate, Long id) {
        return this.stockRepository.findLendableBook(choiceDate, id);
    }

    @Transactional
    public List<String> findBookStockAvailable(Long id) {
        return this.stockRepository.findBookStockAvailable(id);
    }
    
    @Transactional
	public List<BookMst> findByBookTitle(String title){
        return this.bookMstRepository.findByBookTitle(title);
    }

    @Transactional
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }

    @Transactional
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            // 既存レコード取得
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            RentalManage updateTRental = findById(id);
            if (updateTRental == null) {
                throw new Exception("RentalManage record not found.");
            }

            updateTRental.setAccount(account);
            updateTRental.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            updateTRental.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            updateTRental.setStock(stock);
            updateTRental.setStatus(rentalManageDto.getStatus());

            // データベースへの保存
            this.rentalManageRepository.save(updateTRental);
        } catch (Exception e) {
            throw e;
        }
    }

    public String check(Long id,RentalManageDto rentalManageDto) {

        String newStockId = rentalManageDto.getStockId();
        // 在庫管理番号に紐づいたステータスのうち「0」か「1」の情報を持ってくる
        List<RentalManage> statusList = this.findByStockIdAndStatus(newStockId);
        // 取得したデータが0件だった場合
        if (statusList == null) {

            return "この本は新規で貸出登録してください";
        }
        // ステータスが「0」か「1」の場合を比べる
        Date newExRentaledAt = rentalManageDto.getExpectedRentalOn();
        Date newExReturnedAt = rentalManageDto.getExpectedReturnOn();

        for (RentalManage List : statusList) {
            Date exRentaledAt = List.getExpectedRentalOn();
            Date exReturnedAt = List.getExpectedReturnOn();

            if (id != List.getId()) {
                if (!(newExReturnedAt.before(exRentaledAt)) && !(exReturnedAt.before(newExRentaledAt))) {
                    String errorMessage = "現在この書籍は利用中のため貸出できません";

                    return errorMessage;

                }

            }
        }
        return null;
    }

    public String getStockId (Date choiceDate, String title) {

        /*
         * １、タイトルからbook_id
         * ２、１のidとbookmstのbookidに紐づく、かつ削除フラグnull在庫管理番号すべて
         * ３、貸出ステータスが0,1かつ"rentalDay"かぶっているの全レコード取得
         * ４，貸出可能な在庫情報=総利用可能在庫情報-３で取得してきた値
         */

        List<BookMst> bookInfos = this.bookMstRepository.findByBookTitle(title);
        //貸出可不可両方持ってる
        List<String> bookStockAvailables = this.stockRepository.findBookStockAvailable(bookInfos.get(0).getId());
        //貸出中のデータ取ってる
        List<String> lendableBooks = this.stockRepository.findLendableBook(choiceDate,bookInfos.get(0).getId());

        //スマートなのはlendableBooks
        //貸出中の書籍が0件だったらfor文の中を飛ばせる
        for(String stockId : lendableBooks){

            //入ってたら削除
            if(bookStockAvailables.contains(stockId)){
                bookStockAvailables.remove(stockId);
            }
        }

        //貸出可能なstockIdを保持しているリストになっているので1つ目を返す
        return bookStockAvailables.get(0);
    }

}