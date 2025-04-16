package pt.feup.industrial.erpsystem.service;

import pt.feup.industrial.erpsystem.dto.ClientOrderRequestDto;

public interface OrderService {
    void processAndSaveOrder(ClientOrderRequestDto orderRequest);
}
