package com.example.room_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String city;
	private Integer capacity;

	@Enumerated(EnumType.STRING)
	private RoomType type;

	@Column(precision = 10, scale = 2)
	private BigDecimal hourlyRate;

	private boolean available = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public RoomType getType() {
		return type;
	}

	public void setType(RoomType type) {
		this.type = type;
	}

	public BigDecimal getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(BigDecimal hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
}
