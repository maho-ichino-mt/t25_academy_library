package jp.co.metateam.library.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add";
    }

    @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra, Model model){
        String title = bookMstDto.getTitle();
        String isbn = bookMstDto.getIsbn();

        boolean errTitleNull = false;
        boolean errIsbnNull = false;
        boolean errTitleCharacterCount = false;
        boolean errIsbnCharacterCount = false;
        boolean errIsbnCharacterType = false;

        List<String> errTitleList = new ArrayList<>();
        List<String> errIsbnList = new ArrayList<>();

        if(title == "" || title == null){
            errTitleList.add("書籍名は必須です");
            errTitleNull = true;
        }
        if(isbn == "" || isbn == null){
            errIsbnList.add("ISBNは必須です");
            errIsbnNull = true;
        }
        if(title.length() > 255){
            errTitleList.add("書籍名は255文字以内で入力してください");
            errTitleCharacterCount = true;
        }
        if(isbn.length() != 13){
            errIsbnList.add("ISBNは13桁で入力してください");
            errIsbnCharacterCount = true;
        }

        // isbnが数値かどうかチェック
        String regex_num = "^[0-9]+$" ;
        Pattern p1 = Pattern.compile(regex_num); // 正規表現パターンの読み込み
        Matcher m1 = p1.matcher(isbn); // パターンと検査対象文字列の照合
        boolean isbnCharacterType = m1.matches();
        if(!isbnCharacterType){
            errIsbnList.add("ISBNは半角数字で入力してください");
            errIsbnCharacterType = true;
        }

        if(errTitleNull || errIsbnNull || errTitleCharacterCount || errIsbnCharacterCount || errIsbnCharacterType){
                model.addAttribute("errtitle", errTitleList);
                model.addAttribute("errisbn", errIsbnList);
            
            
            return "book/add";
        }

        if(!bookMstService.isbnDuplicateCheck(isbn)){
            model.addAttribute("errisbn", "登録済みのISBNです");
            return "book/add";
        }
    
        bookMstService.save(bookMstDto);

        return "redirect:/book/index";
    }
    
}
