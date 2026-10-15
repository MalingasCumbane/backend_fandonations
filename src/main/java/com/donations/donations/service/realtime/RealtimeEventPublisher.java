package com.donations.donations.service.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notifica todos os fãs e visitantes na página pública do criador
     * Destino: /topic/public/creators/{username} e /topic/public/creators/@{username}
     */
    public void notifyPublicCreator(String username, Object payload) {
        if (username == null || username.isBlank()) return;
        try {
            String clean = username.startsWith("@") ? username.substring(1).trim() : username.trim();
            log.info("Enviando evento público em tempo real para: /topic/public/creators/{}", clean);
            messagingTemplate.convertAndSend("/topic/public/creators/" + clean, payload);
            messagingTemplate.convertAndSend("/topic/public/creators/@" + clean, payload);
        } catch (Exception e) {
            log.error("Erro ao enviar WebSocket para {}: {}", username, e.getMessage(), e);
        }
    }

    /**
     * Notifica o criador no seu canal privado (específico pelo seu creatorId)
     * Destino: /topic/creators/{creatorId}/donations
     */
    public void notifyCreatorDonation(UUID creatorId, Object donation) {
        if (creatorId == null) return;
        try {
            String destination = "/topic/creators/" + creatorId + "/donations";
            log.info("Notificando criador de nova doação: {}", destination);
            messagingTemplate.convertAndSend(destination, donation);
        } catch (Exception e) {
            log.error("Erro ao notificar criador {}: {}", creatorId, e.getMessage(), e);
        }
    }

    /**
     * Notifica o criador sobre actualizações de levantamentos (pedidos, aprovações, cancelamentos)
     * Destino: /topic/creators/{creatorId}/payouts
     */
    public void notifyCreatorPayout(UUID creatorId, Object payout) {
        if (creatorId == null) return;
        try {
            String destination = "/topic/creators/" + creatorId + "/payouts";
            log.info("Notificando criador de levantamento: {}", destination);
            messagingTemplate.convertAndSend(destination, payout);
        } catch (Exception e) {
            log.error("Erro ao notificar levantamento para criador {}: {}", creatorId, e.getMessage(), e);
        }
    }

    /**
     * Notifica o criador sobre alterações nas campanhas (criação, edição, cancelamento)
     * Destino: /topic/creators/{creatorId}/campaigns
     */
    public void notifyCreatorCampaign(UUID creatorId, Object campaign) {
        if (creatorId == null) return;
        try {
            String destination = "/topic/creators/" + creatorId + "/campaigns";
            log.info("Notificando criador de alteração em campanha: {}", destination);
            messagingTemplate.convertAndSend(destination, campaign);
        } catch (Exception e) {
            log.error("Erro ao notificar campanha do criador {}: {}", creatorId, e.getMessage(), e);
        }
    }

    /**
     * Notifica o criador com estatísticas atualizadas em tempo real
     * Destino: /topic/creators/{creatorId}/stats
     */
    public void notifyCreatorStats(UUID creatorId, Object stats) {
        if (creatorId == null) return;
        try {
            String destination = "/topic/creators/" + creatorId + "/stats";
            messagingTemplate.convertAndSend(destination, stats);
        } catch (Exception e) {
            log.error("Erro ao notificar estatísticas do criador {}: {}", creatorId, e.getMessage(), e);
        }
    }

    /**
     * Notifica o painel administrativo sobre novos pedidos de verificação ou moderação
     * Destino: /topic/admin/verifications
     */
    public void notifyAdminVerifications(Object payload) {
        try {
            log.info("Notificando admin de novo pedido de verificação");
            messagingTemplate.convertAndSend("/topic/admin/verifications", payload);
        } catch (Exception e) {
            log.error("Erro ao notificar admin: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica o painel administrativo sobre novos pedidos de levantamento
     * Destino: /topic/admin/payouts
     */
    public void notifyAdminPayouts(Object payload) {
        try {
            log.info("Notificando admin de novo pedido de levantamento");
            messagingTemplate.convertAndSend("/topic/admin/payouts", payload);
        } catch (Exception e) {
            log.error("Erro ao notificar admin de levantamento: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica o painel administrativo sobre novos criadores registados ou com perfis atualizados
     * Destino: /topic/admin/creators
     */
    public void notifyAdminCreators(Object payload) {
        try {
            log.info("Notificando admin de criador");
            messagingTemplate.convertAndSend("/topic/admin/creators", payload);
        } catch (Exception e) {
            log.error("Erro ao notificar admin de criadores: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica o painel administrativo sobre novas doações liquidadas globalmente
     * Destinos: /topic/admin/transactions e /topic/admin/donations
     */
    public void notifyAdminTransactions(Object transaction) {
        try {
            log.info("Notificando admin de nova transação global");
            messagingTemplate.convertAndSend("/topic/admin/transactions", transaction);
            messagingTemplate.convertAndSend("/topic/admin/donations", transaction);
        } catch (Exception e) {
            log.error("Erro ao notificar admin de transação: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica o painel administrativo com estatísticas globais atualizadas
     * Destino: /topic/admin/stats
     */
    public void notifyAdminStats(Object stats) {
        try {
            log.info("Notificando admin de estatísticas globais atualizadas");
            messagingTemplate.convertAndSend("/topic/admin/stats", stats);
        } catch (Exception e) {
            log.error("Erro ao notificar admin de stats: {}", e.getMessage(), e);
        }
    }
}
