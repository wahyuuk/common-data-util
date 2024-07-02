package co.id.kuncoro.commondatautil.util;

import static co.id.kuncoro.commonapi.constant.Constants.COMMA;
import static co.id.kuncoro.commonapi.constant.Constants.PERCENT_STR;
import static co.id.kuncoro.commonapi.constant.FilterOperator.numberOperators;
import static co.id.kuncoro.commonapi.constant.FilterOperator.stringOperators;

import co.id.kuncoro.commonapi.constant.FilterOperator;
import co.id.kuncoro.commonapi.dto.Filter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.jpa.domain.Specification;

public class FilterUtils {

  private FilterUtils() {
  }

  public static <E> Set<Filter> getFilter(HttpServletRequest servletRequest, Class<E> entityClass) {
    var queryParams = servletRequest.getParameterMap();
    var fields = FieldUtils.getAllFieldsList(entityClass)
        .stream()
        .map(Field::getName)
        .toList();
    return CollectionUtils.emptyIfNull(queryParams.entrySet())
        .stream()
        .filter(e -> CollectionUtils.containsAny(fields, e.getKey()))
        .filter(e -> e.getValue() != null && e.getValue().length >= 1)
        .filter(e -> {
          var query = ObjectUtils.defaultIfNull(e.getValue()[0], StringUtils.EMPTY);
          var queryValues = StringUtils.split(query, COMMA);
          return isParseable(entityClass, e.getKey(), queryValues[0]);
        })
        .map(e -> {
          var query = ObjectUtils.defaultIfNull(e.getValue()[0], StringUtils.EMPTY);
          var queryValues = StringUtils.split(query, COMMA);
          var operator = FilterOperator.EQUALS;
          if (queryValues.length > 1) {
            operator = EnumUtils.getEnum(FilterOperator.class, queryValues[1], FilterOperator.EQUALS);
          }

          return Filter.builder()
              .field(e.getKey())
              .value(queryValues[0])
              .operator(operator)
              .build();
        })
        .collect(Collectors.toSet());
  }

  public static <E> Specification<E> specification(Set<Filter> filters, Class<E> entityClass) {
    var specifications = filters
        .stream()
        .map(e -> specification(e, entityClass))
        .toList();
    return Specification.allOf(specifications);
  }

  private static <E> boolean isParseable(Class<E> entityClass, String fieldName, String value) {
    try {
      FieldUtils.writeDeclaredStaticField(entityClass, fieldName, value);
      return true;
    } catch (IllegalAccessException e) {
      return false;
    }
  }

  private static <E> Specification<E> specification(Filter filter, Class<E> entityClass) {
    return (root, query, criteriaBuilder) -> toPredicate(filter, entityClass, root, criteriaBuilder);
  }

  private static <E> Predicate toPredicate(Filter filter, Class<E> entityClass, Path<E> path,
      CriteriaBuilder criteria) {
    var field = FieldUtils.getField(entityClass, filter.getField(), true);
    if (filter.getOperator() == null) {
      filter.setOperator(FilterOperator.EQUALS);
    }

    if (Number.class.isAssignableFrom(field.getType()) && !numberOperators().contains(filter.getOperator())) {
      filter.setOperator(FilterOperator.EQUALS);
    }

    if (String.class.isAssignableFrom(field.getType()) && !stringOperators().contains(filter.getOperator())) {
      filter.setOperator(FilterOperator.EQUALS);
    }

    if (Boolean.class.isAssignableFrom(field.getType())) {
      filter.setOperator(FilterOperator.EQUALS);
    }

    if (field.getType().isEnum()) {
      filter.setOperator(FilterOperator.EQUALS);
    }

    return toPredicate(filter, path, criteria);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static <E> Predicate toPredicate(Filter filter, Path<E> path, CriteriaBuilder criteria) {
    var field = filter.getField();
    var value = filter.getValue();
    return switch (filter.getOperator()) {
      case EQUALS -> criteria.equal(path.get(field), value);
      case NOT_EQUALS -> criteria.notEqual(path.get(field), value);
      case LIKE -> criteria.like(path.get(field), StringUtils.join(PERCENT_STR, value, PERCENT_STR));
      case START_WITH -> criteria.like(path.get(field), StringUtils.join(PERCENT_STR, value));
      case END_WITH -> criteria.like(path.get(field), StringUtils.join(value, PERCENT_STR));
      case GREATER_THAN -> criteria.greaterThan(path.get(field), (Comparable) value);
      case LESS_THAN -> criteria.lessThan(path.get(field), (Comparable) value);
      case GREATER_THAN_OR_EQUALS -> criteria.greaterThanOrEqualTo(path.get(field), (Comparable) value);
      case LESS_THAN_OR_EQUALS -> criteria.lessThanOrEqualTo(path.get(field), (Comparable) value);
      case IN -> path.get(field).in(value);
      case NOT_IN -> path.get(field).in(value).not();
      default -> criteria.equal(path, path);
    };
  }

}
