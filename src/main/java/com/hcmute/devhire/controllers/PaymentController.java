package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.PaymentDTO;
import com.hcmute.devhire.DTOs.PaymentQueryDTO;
import com.hcmute.devhire.DTOs.PaymentRefundDTO;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.responses.ResponseObject;
import com.hcmute.devhire.services.IPaymentService;
import com.hcmute.devhire.services.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final IVNPayService vnPayService;
    private final IPaymentService paymentService;

    @PostMapping("/create_payment_url")
    public ResponseEntity<ResponseObject> createPayment(@RequestBody PaymentDTO paymentRequest, HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(paymentRequest, request);

            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Payment URL generated successfully.")
                    .data(paymentUrl)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error generating payment URL: " + e.getMessage())
                            .build());
        }
    }
    @PostMapping("/query")
    public ResponseEntity<ResponseObject> queryTransaction(@RequestBody PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) {
        try {
            String result = vnPayService.queryTransaction(paymentQueryDTO, request);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Query successful")
                    .data(result)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error querying transaction: " + e.getMessage())
                    .build());
        }
    }
    @PostMapping("/refund")
    public ResponseEntity<ResponseObject> refundTransaction(
            @Valid @RequestBody PaymentRefundDTO paymentRefundDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message(String.join(", ", errorMessages))
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .build());
        }

        try {
            String response = vnPayService.refundTransaction(paymentRefundDTO);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Refund processed successfully")
                    .status(HttpStatus.OK)
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                    .message("Failed to process refund: " + e.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/payment-callback")
    public ResponseEntity<?> handlePaymentCallback(HttpServletRequest request) {
        String username = JwtUtil.getAuthenticatedUsername();
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            String subscriptionId = request.getParameter("vnp_OrderInfo");
            paymentService.completePayment(username, Long.valueOf(subscriptionId));
            String successHtml = "<html><head><link href=\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,400i,700,900&display=swap\" rel=\"stylesheet\"></head><style>body {text-align: center; padding: 40px 0; background: #EBF0F5; } h1 { color: #88B04B; font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif; font-weight: 900; font-size: 40px; margin-bottom: 10px; } p { color: #404F5E; font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif; font-size:20px; margin: 0; } i { color: #9ABC66; font-size: 100px; line-height: 200px; margin-left:-15px; } .card { background: white; padding: 60px; border-radius: 4px; box-shadow: 0 2px 3px #C8D0D8; display: inline-block; margin: 0 auto; } </style> <body> <div class=\"card\"> <div style=\"border-radius:200px; height:200px; width:200px; background: #F8FAF5; margin:0 auto;\"> <i class=\"checkmark\">✓</i> </div> <h1>Thanh toán thành công</h1> <p>Cảm ơn bạn đã mua sắp tại cửa hàng của chúng tôi!<br/> Thông báo này sẽ đóng lại sau vài giây !</p> </div> <script>setTimeout(function() { window.close(); }, 10000);</script> </body> </html>";
            return new ResponseEntity<>(successHtml, HttpStatus.OK);
        } else {
            String errorHtml = "<html>\n" +
                    "  <head>\n" +
                    "    <link href=\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,400i,700,900&display=swap\" rel=\"stylesheet\">\n" +
                    "  </head>\n" +
                    "    <style>\n" +
                    "      body {\n" +
                    "        text-align: center;\n" +
                    "        padding: 40px 0;\n" +
                    "        background: #EBF0F5;\n" +
                    "        font-family: system-ui;\n" +
                    "        background: #D8FDD8;\n" +
                    "        color: white;\n" +
                    "        text-align: center;\n" +
                    "      }\n" +
                    "        h1 {\n" +
                    "          color: #e42525;\n" +
                    "          font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif;\n" +
                    "          font-weight: 700;\n" +
                    "          font-size: 20px;\n" +
                    "          margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        p {\n" +
                    "          color: #404F5E;\n" +
                    "          font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif;\n" +
                    "          font-size:20px;\n" +
                    "          margin: 0;\n" +
                    "        }\n" +
                    "      i {\n" +
                    "        color: #dd2722;\n" +
                    "        font-size: 100px;\n" +
                    "        line-height: 200px;\n" +
                    "        margin-left:-15px;\n" +
                    "      }\n" +
                    "      .card {\n" +
                    "        background: white;\n" +
                    "        padding: 60px;\n" +
                    "        border-radius: 4px;\n" +
                    "        box-shadow: 0 2px 3px #C8D0D8;\n" +
                    "        display: inline-block;\n" +
                    "        margin: 0 auto;\n" +
                    "      }\n" +
                    "    </style>\n" +
                    "    <body>\n" +
                    "      <div class=\"card\">\n" +
                    "      <div style=\"border-radius:200px; height:200px; width:200px; background: #f5bebe; margin:0 auto;\">\n" +
                    "        <i class=\"checkmark\">✘</i>\n" +
                    "      </div>\n" +
                    "        <h1>Thanh toán không thành công.</h1> \n" +
                    "        <p>Vui lòng kiểm tra lại phương thức thanh toán !<br/> Thông báo này sẽ tự đóng sau vài giây</p>\n" +
                    "      </div>\n" +
                    "      <script>\n" +
                    "        setTimeout(function() { window.close(); }, 10000);\n" +
                    "      </script>\n" +
                    "    </body>\n" +
                    "</html>";
            return new ResponseEntity<>(errorHtml, HttpStatus.BAD_REQUEST);
        }
    }
}
