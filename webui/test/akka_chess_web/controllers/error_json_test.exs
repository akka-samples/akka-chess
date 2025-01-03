defmodule AkkaChessWeb.ErrorJSONTest do
  use AkkaChessWeb.ConnCase, async: true

  test "renders 404" do
    assert AkkaChessWeb.ErrorJSON.render("404.json", %{}) == %{errors: %{detail: "Not Found"}}
  end

  test "renders 500" do
    assert AkkaChessWeb.ErrorJSON.render("500.json", %{}) ==
             %{errors: %{detail: "Internal Server Error"}}
  end
end
