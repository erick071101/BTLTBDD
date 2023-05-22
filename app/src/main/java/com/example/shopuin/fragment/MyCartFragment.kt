package com.example.shopuin.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopuin.R
import com.example.shopuin.adapter.CartListAdapter
import com.example.shopuin.control.FirestoreClass
import com.example.shopuin.databinding.FragmentMycartBinding
import com.example.shopuin.models.CartItem
import com.example.shopuin.models.Products

class MyCartFragment : BaseFragment() {


    private var _binding: FragmentMycartBinding? = null
    private lateinit var mProductsList: ArrayList<Products>
    private lateinit var mCartListItems: ArrayList<CartItem>
    private val binding get() = _binding!!




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMycartBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun itemUpdateSuccess() {
        hideProgressDialog()
        getCartItemsList()
    }

    fun itemRemovedSuccess() {
        hideProgressDialog()
        Toast.makeText(
            context,
            "Xoá sản phẩm thành công",
            Toast.LENGTH_SHORT
        ).show()
        getCartItemsList()

    }

    private fun getCartItemsList() {
        showProgressDialog("Loading")
        FirestoreClass().getCartList(this)


    }


    private fun getProductList() {
        FirestoreClass().getAllProductsList(this)
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Products>) {
        mProductsList = productsList
        getCartItemsList()

    }

    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hideProgressDialog()
        for (product in mProductsList) {
            for (cartItem in cartList) {
                if (product.product_id == cartItem.product_id) {
                    cartItem.stock_quantity = product.stock_quantity
                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }
        mCartListItems = cartList
        val fragment = this
        if (mCartListItems.size > 0) {
            binding.rvCartItemsList.visibility = View.VISIBLE
            binding.llCheckout.visibility = View.VISIBLE
            with(binding.rvCartItemsList) {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                val cartListAdapter = CartListAdapter(
                    context,fragment,
                    cartListItems = mCartListItems, true
                )
                adapter = cartListAdapter
            }

            var subTotal: Double = 0.0
            var shippingCharge = 0

            for (item in mCartListItems) {
                val availableQuantity = item.stock_quantity.toInt()
                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()
                    shippingCharge = item.product_shipping_charge.toInt()
                    subTotal += (price * quantity)
                }

            }

            binding.tvSubTotal.text = "$$subTotal"
            binding.tvShippingCharge.text = "$$shippingCharge"

            if (subTotal > 0) {
                binding.llCheckout.visibility = View.VISIBLE
                val total = subTotal + shippingCharge

                binding.tvTotalAmount.text = "$$total"
            } else {

                binding.llCheckout.visibility = View.GONE


            }

        } else {

        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onResume() {
        super.onResume()
        getProductList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}