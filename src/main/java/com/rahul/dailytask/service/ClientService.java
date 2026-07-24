package com.rahul.dailytask.service;

import com.rahul.dailytask.entity.Client;
import com.rahul.dailytask.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Get All Clients
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // Get Client By ID
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    // Save Client (Create New)
    public Client saveClient(Client client) {
        // Set last updated timestamp
        client.setLastUpdated(LocalDateTime.now().format(FORMATTER));
        if (client.getStatus() == null || client.getStatus().isEmpty()) {
            client.setStatus("200");
        }
        return clientRepository.save(client);
    }

    // Update Client - FIXED
    public Client updateClient(Long id, Client clientDetails) {
        System.out.println("📥 UPDATE REQUEST for ID: " + id);
        System.out.println("📝 Client details received: " + clientDetails);

        // Find existing client
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        System.out.println("📝 Existing client before update: " + existingClient);

        // Update fields only if they are not null
        if (clientDetails.getClientName() != null && !clientDetails.getClientName().isEmpty()) {
            existingClient.setClientName(clientDetails.getClientName());
        }
        if (clientDetails.getDashboardUrl() != null) {
            existingClient.setDashboardUrl(clientDetails.getDashboardUrl());
        }
        if (clientDetails.getUatWhatsapp() != null) {
            existingClient.setUatWhatsapp(clientDetails.getUatWhatsapp());
        }
        if (clientDetails.getProdWhatsapp() != null) {
            existingClient.setProdWhatsapp(clientDetails.getProdWhatsapp());
        }
        if (clientDetails.getClientNumber() != null) {
            existingClient.setClientNumber(clientDetails.getClientNumber());
        }
        if (clientDetails.getStatus() != null) {
            existingClient.setStatus(clientDetails.getStatus());
        }
        if (clientDetails.getUatDashboardUrl() != null) {
            existingClient.setUatDashboardUrl(clientDetails.getUatDashboardUrl());
        }
        if (clientDetails.getProdLabUrl() != null) {
            existingClient.setProdLabUrl(clientDetails.getProdLabUrl());
        }
        if (clientDetails.getUatLabUrl() != null) {
            existingClient.setUatLabUrl(clientDetails.getUatLabUrl());
        }
        
        // Always update the timestamp
        existingClient.setLastUpdated(LocalDateTime.now().format(FORMATTER));

        // Save the updated client
        Client updatedClient = clientRepository.save(existingClient);
        System.out.println("✅ Client updated successfully: " + updatedClient);
        System.out.println("📝 Updated data: " + updatedClient);

        return updatedClient;
    }

    // Delete Client
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
        System.out.println("🗑️ Client deleted with ID: " + id);
    }
}