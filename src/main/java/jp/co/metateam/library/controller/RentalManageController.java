package jp.co.metateam.library.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Stock;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller

public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
    private String rentalErrorAdd;
    private String rentalErrorEdit;

    @Autowired
    public RentalManageController(
            AccountService accountService,
            RentalManageService rentalManageService,
            StockService stockService) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * 
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得

        List<RentalManage> rentalManageList = this.rentalManageService.findAll();

        // 貸出一覧画面に渡すデータをmodelに追加

        model.addAttribute("rentalManageList", rentalManageList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        // 貸出一覧画面に遷移
        return "rental/index";
    }
    

    @GetMapping("/rental/statusFilter")
    public String statusFilter(@RequestParam(name = "status_filter", required = false) Integer status, Model model) {
        List<RentalManage> rentalManageList;
    
        // ステータスが指定されている場合
        if (status != null) {
            rentalManageList = this.rentalManageService.getFilter(status);
            
            // 指定されたステータスの件数が0件の場合
            if (rentalManageList.isEmpty()) {
                model.addAttribute("errorMessage","選択されたステータス情報はありません");
            }
        } else {
            // ステータスが指定されていない場合、すべての貸出情報を取得
            rentalManageList = this.rentalManageService.findAll();
        }
    
       model.addAttribute("rentalManageList", rentalManageList);

        return "rental/index";        
    }


    
    @GetMapping("/rental/add")
    public String add(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date rentalDay,
            @RequestParam(required = false) String title, Model model) {
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accounts = this.accountService.findAll();

        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("stockList", stockList);
        model.addAttribute("accounts", accounts);

        if (rentalDay != null && title != null) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            String stockId = this.rentalManageService.getStockId(rentalDay, title);
            rentalManageDto.setExpectedRentalOn(rentalDay);
            rentalManageDto.setStockId(stockId);

            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,RedirectAttributes ra) {
        try {

            Long addOtherReservations = rentalManageService.addOtherReservations(rentalManageDto.getStockId());
            Long addOtherDates = rentalManageService.addOtherDates(rentalManageDto.getStockId(),
                    rentalManageDto.getExpectedReturnOn(), rentalManageDto.getExpectedRentalOn());

            if (addOtherReservations != addOtherDates) {
                rentalErrorAdd = "この期間は貸出できません";
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalErrorAdd));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", rentalErrorAdd));
            }

            String returnDateError = rentalManageDto.isReturnDateError(rentalManageDto);
            if(returnDateError !=null){
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", returnDateError));
                throw new RuntimeException();
            }

            if (result.hasErrors()) {
                throw new Exception("Stock unavailable error");
            }

            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        List<Stock> stockList = this.stockService.findAll(); // 在庫管理番号のプルダウンリスト作成
        List<Account> accounts = this.accountService.findAll(); // 社員番号のプルダウンリスト作成

        model.addAttribute("stockList", stockList); // 在庫管理番号のリストを表示（プルダウン）
        model.addAttribute("accounts", accounts); // 社員番号のリストを表示（プルダウン）
        model.addAttribute("rentalStatus", RentalStatus.values()); // 貸出ステータスリスト（プルダウン）

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());

            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        return "rental/edit";

    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
    try {
        RentalManage rentalManege = this.rentalManageService.findById(id);
        String errMsgOfStatus =
        rentalManageDto.validateStatus(rentalManege.getStatus());

        if (errMsgOfStatus!=null) {
            result.addError(new
            FieldError("rentalManageDto","status",errMsgOfStatus));
        }
        //在庫DBから在庫管理番号を取得
        Stock stock=this.stockService.findById(rentalManageDto.getStockId());

        //紐づく在庫ステータス全レコードを取得
        int stockStatus = stock.getStatus();
        //比べる
        if(stockStatus ==1){
            FieldError fieldError = new FieldError("rentalManageDto", "stockId", "現在この書籍は貸出できません");
            result.addError(fieldError);
            throw new Exception("Stock error.");
        }

        Integer status = rentalManageDto.getStatus();

        String stockId = rentalManageDto.getStockId();

        Long rentalSumEdit = this.rentalManageService.otherReservations(stockId,id);

        if (status == 0 || status == 1) {
            if (!(rentalSumEdit == 0)) {
                Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
                Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
                Long rentalNum = this.rentalManageService.otherDates(stockId, id, expectedReturnOn, expectedRentalOn);

                if (!(rentalSumEdit == rentalNum)) {
                    rentalErrorEdit = "この期間は貸出できません";
                    result.addError(new FieldError("rentalManage", "expectedRentalOn", rentalErrorEdit));
                    result.addError(new FieldError("rentalManage", "expectedReturnOn", rentalErrorEdit));
                }
            }
        }

        String returnDateError = rentalManageDto.isReturnDateError(rentalManageDto);
        if(returnDateError !=null){
            result.addError(new FieldError("rentalManageDto", "expectedReturnOn", returnDateError));
            throw new RuntimeException();
        }
        
        if(result.hasErrors()){
            throw new Exception("Validation error.");
        }

        // 更新処理
        this.rentalManageService.update(id,rentalManageDto);

        return "redirect:/rental/index";

        } catch (Exception e) {
            log.error(e.getMessage());
    
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);
    
            List<Account> accounts = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findAll();
    
            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("stockList", stockList);
            model.addAttribute("accounts", accounts);
    
            return "rental/edit";
        }
    }

}
