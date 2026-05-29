package kr.ac.hansung.controller;

import kr.ac.hansung.dto.ProductDto;
import kr.ac.hansung.entity.Product;
import kr.ac.hansung.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 과제 필수: 페이징 및 키워드 검색 통합 처리
    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) { //

        // id 역순 또는 정순 정렬 기반의 페이지 요청 객체 생성 (기본값 5개씩 조회)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id")); //

        // 빈 문자열("")이 들어오면 null로 처리하여 파라미터 깔끔하게 정규화
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null; //

        Page<Product> productPage; //
        if (normalizedKeyword != null) {
            // 검색어가 채워져 있으면 검색 로직 수행
            productPage = productService.searchProducts(normalizedKeyword, pageRequest); //
        } else {
            // 검색어가 없으면 일반 전체 페이징 목록 조회
            productPage = productService.getProducts(pageRequest); //
        }

        // Thymeleaf 뷰로 페이징 뭉치 데이터(productPage)와 키워드 전달
        model.addAttribute("productPage", productPage); //
        model.addAttribute("keyword", normalizedKeyword); //

        return "products/list"; //
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/detail";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("product", new ProductDto());
        return "products/add";
    }

    @PostMapping
    public String save(@ModelAttribute ProductDto dto) {
        productService.save(dto);
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/products";
    }
}