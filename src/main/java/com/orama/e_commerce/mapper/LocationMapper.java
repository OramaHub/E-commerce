package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.location.CityResponseDto;
import com.orama.e_commerce.dtos.location.CitySimpleDto;
import com.orama.e_commerce.dtos.location.CountryResponseDto;
import com.orama.e_commerce.dtos.location.StateResponseDto;
import com.orama.e_commerce.models.City;
import com.orama.e_commerce.models.Country;
import com.orama.e_commerce.models.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {

  CountryResponseDto toCountryDto(Country country);

  StateResponseDto toStateDto(State state);

  CityResponseDto toCityDto(City city);

  @Mapping(target = "stateId", source = "state.id")
  @Mapping(target = "stateName", source = "state.name")
  @Mapping(target = "stateCode", source = "state.abbreviation")
  CitySimpleDto toCitySimpleDto(City city);
}
