package kr.ac.hansung.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.dto.ProductDto;
import kr.ac.hansung.entity.Product;
import kr.ac.hansung.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            Model model) {

        // id 정순 정렬 기반의 페이지 요청 객체 생성 (기본값 5개씩 조회)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id"));

        // 빈 문자열("")이 들어오면 null로 처리하여 파라미터 깔끔하게 정규화
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;

        Page<Product> productPage;
        if (normalizedKeyword != null) {
            // 검색어가 채워져 있으면 검색 로직 수행
            productPage = productService.searchProducts(normalizedKeyword, pageRequest);
        } else {
            // 검색어가 없으면 일반 전체 페이징 목록 조회
            productPage = productService.getProducts(pageRequest);
        }

        // Thymeleaf 뷰로 페이징 데이터(productPage)와 키워드 전달
        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", normalizedKeyword);

        return "products/list";
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

    // 과제 필수: 상품 수정 폼 표시 — 기존 데이터 pre-fill
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);

        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setStock(product.getStock());

        model.addAttribute("product", dto);
        model.addAttribute("productId", id);
        return "products/edit";
    }

    // 과제 필수: 상품 수정 처리 — @Valid 검증 + 더티 체킹 + Flash 메시지
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("product") ProductDto dto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes ra) {

        // @Valid 검증 실패 시 수정 폼으로 복귀
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            return "products/edit";
        }

        // ProductService.updateProduct() 내부에서 더티 체킹으로 자동 UPDATE
        productService.updateProduct(id, dto);
        ra.addFlashAttribute("successMessage", "상품이 수정되었습니다.");
        return "redirect:/products";
    }
}