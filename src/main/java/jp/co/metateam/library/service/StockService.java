package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,RentalManageRepository rentalManageRepository){
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<BookMst> findAllBookData() {
        List<BookMst> findAllBookData = this.bookMstRepository.findAllBookData();

        return  findAllBookData;
    }

    @Transactional
    public List<Stock> findAllAvailableStockData(Long bookId) {
        return this.stockRepository.findAllAvailableStockData(bookId);
    }

    // @Transactional
    // public List<Stock> lendableBook(Date choiceDate, Long id) {
    //     return this.stockRepository.lendableBook(choiceDate, id);
    // }

    // @Transactional
    // public List<Stock> bookStockAvailable(Long id) {
    //     return this.stockRepository.bookStockAvailable(id);
    // }

    @Transactional
    public Long scheduledRentaWaitData(Date day, List<String>stock_id) {
        return this.rentalManageRepository.scheduledRentaWaitData(day, stock_id);
    }

    @Transactional
    public long scheduledRentalingData(Date date, List<String> stockId) {
      List<RentalManage> unavailableStockLists = this.rentalManageRepository.scheduledRentalingData(date, stockId);
      long unavailableStockNum =unavailableStockLists.size();
 
      return unavailableStockNum;
    }

    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<List<String>> generateValues(Integer year, Integer month, Integer daysInMonth) {
        //2次元のStringをつめるbigValuesっていう箱を作る
        List<List<String>> bigValues = new ArrayList<>();
        //BookMstテーブルのすべての書籍情報をリストに追加
        List<BookMst> bookData = findAllBookData();
        LocalDate today = LocalDate.now();
    
        //書籍数分ループ
        for (BookMst book : bookData){
            //bigValuesっていう大きな箱の中の各書籍の情報を入れるbookInfoって箱
            List<String> bookInfo = new ArrayList<>();
            //bookInfoって箱にループしてきたタイトルをつめてる
            bookInfo.add(book.getTitle());

            //利用可能総在庫数
            List<Stock> availableStocks = findAllAvailableStockData(book.getId());
            //数字を文字列に変換　　　　　　　　　　　　　　↓.sizeでavailableStocksの中身を数えてる
            String availableStocksCount = String.valueOf(availableStocks.size());
            //数えたやつをbookInfoに追加
            bookInfo.add(availableStocksCount);


            List<String> stockIdList = new ArrayList<>();
            for(Stock stock : availableStocks) {
             //stockIdって箱にループしてきたIdをつめてる
                stockIdList.add(stock.getId());
            }
            

            //日付分ループ
             for(int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++){
                //対象の日付を取得
                LocalDate localDate = LocalDate.of(year,month,dayOfMonth);
                //LocalDate型のlocalDateををDate型に変換しdateに入れる
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                //日ごとの利用可能在庫数
                Long scheduledRentaWaitDataCount = scheduledRentaWaitData(date,stockIdList);
                Long scheduledRentalingDataCount = scheduledRentalingData(date,stockIdList);

                Long total =  availableStocks.size() - (scheduledRentaWaitDataCount + scheduledRentalingDataCount);
                //計算してtotalに入れたデータを String型のtotalValueに変換
                String totalValue = Long.toString(total);

                if (today.isAfter(localDate)) {
                    totalValue = "0";
                }
                
                //取得した日付ごとの利用可能在庫数をbookInfoに入れる
                bookInfo.add(totalValue);
                //bookInfo.addAll(stockIdList);
             }

            bigValues.add(bookInfo);

        }
        return bigValues;
    }
}

 
