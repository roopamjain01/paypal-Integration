package com.example.payment;


import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaypalController {
    private final PaypalService service;
    private static final String BASE_URL = "http://localhost:9090/";
    private static final String SUCCESS_URL = "pay/success";
    private static final String CANCEL_URL = "pay/cancel";

    @Autowired
    public PaypalController(PaypalService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        System.out.println("home page");
        return "home";
    }

    @PostMapping("/pay")
    // @ModelAttribute : refers to the model object in MVC
    // this is used to display value in FE template (theme-leaf)
    public String payment(@ModelAttribute("order") Order order) {
        try {
            Payment payment = service.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),
                order.getIntent(), order.getDescription(), BASE_URL + CANCEL_URL, BASE_URL + SUCCESS_URL);

            // @Todo need to understand more
            for(Links link:payment.getLinks()) {
                if(link.getRel().equals("approval_url")) {
                    return "redirect:"+link.getHref();
                }
            }

        } catch (PayPalRESTException e) {

            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        return "cancel";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = service.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

}
