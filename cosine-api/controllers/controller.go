package controllers

import (
	"encoding/base64"
	"fmt"
	"path/filepath"
	"webapi/utils"

	"github.com/gofiber/fiber/v2"
)

const FILENAME string = "features"

var storedFeatures []byte

func getFilePath() string {
	p, err := filepath.Abs(filepath.Join("files", FILENAME))
	if err != nil {
		fmt.Println("Error building path:")
		panic(err)
	}
	return p
}

// Take base64 string from "features" inside JSON body, decode it and saves it to a file
func Store(c *fiber.Ctx) error {
	var data map[string]string

	if err := c.BodyParser(&data); err != nil {
		return err
	}

	dst, err := base64.StdEncoding.DecodeString(data["features"])
	if err != nil {
		return err
	}

	storedFeatures = dst

	return c.JSON(fiber.Map{
		"message": "success",
	})
}

// Decode base64 string from "features" inside JSON body, and compare it from previously stored features
func Compare(c *fiber.Ctx) error {
	var data map[string]string

	if err := c.BodyParser(&data); err != nil {
		return err
	}

	if storedFeatures == nil {
		return fiber.NewError(500, "First store some features!")
	}

	received, err := base64.StdEncoding.DecodeString(data["features"])
	if err != nil {
		return err
	}

	cosine := utils.Cosine(storedFeatures, received)

	return c.JSON(fiber.Map{
		"message": "success",
		"cosine":  cosine,
	})
}
