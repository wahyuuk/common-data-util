package co.id.kuncoro.commondatautil.dto;

import co.id.kuncoro.commonapi.dto.Filter;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {

  private List<T> results;
  private PageInfo pageInfo;

  public static <T> PageResponse<T> of(List<T> contents, Set<Filter> filters, Pageable page, long totalItems) {
    var pageInfo = PageInfo.builder()
        .pageSize(page.getPageSize())
        .totalPage(page.getPageNumber())
        .totalItems(totalItems)
        .filters(filters)
        .build();
    return new PageResponse<>(contents, pageInfo);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PageInfo {

    private Integer pageSize;
    private Integer totalPage;
    private Long totalItems;
    private Set<Filter> filters;

  }

}
