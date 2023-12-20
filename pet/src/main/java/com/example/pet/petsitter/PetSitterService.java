package com.example.pet.petsitter;

import com.example.pet.core.error.exception.Exception400;
import com.example.pet.core.error.exception.Exception404;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Transactional(readOnly = true) // 읽기 전용
@RequiredArgsConstructor
@Service
public class PetSitterService {
    private final PetSitterRepository productRepository;

    // 상품저장
    @Transactional
    public PetSitter save(PetSitterResponse.FindAllDTO product) {
        try {
            // DTO 를 엔티티로 변환해서 저장 후 변환
            PetSitter saveProduct = productRepository.save(product.toEntity());
            return saveProduct;
        }catch (Exception e) {
            throw new Exception400("잘못된 요청으로 상품 등록 중 오류가 발생했습니다.");
        }
    }
//
//
//    // 페이징된 상품목록 조회
//    public List<PetSitterResponse.FindAllDTO> findAll(int page) {
//
//        Pageable pageable = PageRequest.of(page,3);
//        Page<PetSitter> productPage = productRepository.findAll(pageable);
//
//        if (productPage.isEmpty()) {
//            // 만약 페이지 내용이 비어 있다면 404 예외를 던집니다.
//            throw new Exception404("해당 페이지에 상품이 없습니다.");
//        }
//
//        // Product Entity - > DTO 변환 해서 리스트로 변환
//        List<PetSitterResponse.FindAllDTO> productDTOS =
//                productPage.getContent().stream().map(PetSitterResponse.FindAllDTO::new)//람다식 - > 기본생성자 호출
//                .collect(Collectors.toList());
//
//        return productDTOS;
//
//    }
//
//    //개별상품 검색
//    public PetSitterResponse.FindByIdDTO findById(Long id) { //상품 하나 안에 재품들이 여러개 일수 있음(+옵션)
//    // DTO안에 LIST 가지고 있기 때문에 바로 DTO반환 (상품이랑 옵션 함께 반환 해야하기 때문에 ProductResponse.FindByIdDTO에서 작업)
//        PetSitter product = productRepository.findById(id).orElseThrow( //예외 처리 바로하기
//                () -> new Exception404("해당 상품을 찾을 수 없습니다. : " + id) // 상품이 없을 경우
//        );
//        // product.getId()로 Option 상품 검색
//         List<Option> optionList = optionRepository.findByProductId(product.getId());
//
//        // ** 검색이 완료된 제품 - 리스트로 반환
//        return new PetSitterResponse.FindByIdDTO(product, optionList); // 생성자 반환
//    }
//
//
//    // 상품 업데이트
//    @Transactional
//    public PetSitter update(Long id, PetSitterResponse.FindByIdDTO productDTO) {
//        try {
//            // 상품 ID로 상품 검색, 없으면 예외 발생
//            PetSitter productUpdate = productRepository.findById(id).orElseThrow( //예외 처리 바로하기
//                    () -> new Exception404("해당 상품을 찾을 수 없습니다. : " + id) // 상품이 없을 경우
//            );
//
//            productUpdate.updateFromDTO(productDTO);
//
//            return productRepository.save(productUpdate);
//
//        } catch (Exception e) {
//            throw new Exception400("잘못된 요청 형식으로 상품을 업데이트하는 중 오류가 발생했습니다.");
//        }
//    }
//
//
//
//    // 상품삭제
//    @Transactional
//    public void delete(Long id) {
//        // 상품 ID로 검색, 없으면 예외 발생
//        PetSitter product = productRepository.findById(id).orElseThrow( //예외 처리 바로하기
//                () -> new Exception404("해당 상품을 찾을 수 없습니다. : " + id) // 상품이 없을 경우
//        );
//        productRepository.delete(product);
//    }


}
